package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Goods;
import com.nuaa.secondhand.mapper.GoodsMapper;
import com.nuaa.secondhand.service.FeedPushService;
import com.nuaa.secondhand.service.GoodsService;
import com.nuaa.secondhand.utils.RedisConstants;
import com.nuaa.secondhand.utils.SimpleRedisLock;
import com.nuaa.secondhand.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.geo.Circle;
import org.springframework.data.geo.Distance;
import org.springframework.data.geo.Metrics;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.geo.GeoResult;
import org.springframework.data.geo.GeoResults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;

/**
 * 商品服务。详情页缓存解决三大问题：
 * 缓存穿透（空值缓存）、缓存击穿（互斥锁重建）、缓存雪崩（TTL 随机）。
 * 另含：附近商品（GeoHash）、UV 统计（HyperLogLog）、Feed 推送。
 */
@Slf4j
@Service
public class GoodsServiceImpl extends ServiceImpl<GoodsMapper, Goods> implements GoodsService {

    private static final String GEO_KEY = "geo:goods";
    private static final String FANS_KEY = "fans:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Resource
    private FeedPushService feedPushService;

    @Override
    public Result detail(Long id) {
        String key = RedisConstants.CACHE_GOODS_KEY + id;
        String json = stringRedisTemplate.opsForValue().get(key);

        // 1. 缓存命中（含空值缓存 "NULL"）
        if (json != null) {
            if ("NULL".equals(json)) {
                return Result.fail("商品不存在或已下架");
            }
            try {
                recordUv(id);
                return Result.ok(objectMapper.readValue(json, Goods.class));
            } catch (Exception e) {
                log.warn("缓存反序列化失败，重建缓存：{}", e.getMessage());
            }
        }

        // 2. 缓存未命中，互斥锁重建（防击穿）
        String lockKey = RedisConstants.LOCK_KEY + key;
        SimpleRedisLock lock = new SimpleRedisLock(stringRedisTemplate, lockKey);
        boolean locked = false;
        try {
            locked = lock.tryLock(10);
            if (!locked) {
                Thread.sleep(50);
                return detail(id);
            }
            json = stringRedisTemplate.opsForValue().get(key);
            if (json != null) {
                if ("NULL".equals(json)) {
                    return Result.fail("商品不存在或已下架");
                }
                recordUv(id);
                return Result.ok(objectMapper.readValue(json, Goods.class));
            }

            // 3. 查数据库
            Goods goods = baseMapper.selectById(id);
            if (goods == null) {
                stringRedisTemplate.opsForValue().set(key, "NULL", RedisConstants.CACHE_NULL_TTL, TimeUnit.MINUTES);
                return Result.fail("商品不存在或已下架");
            }

            // 4. 写缓存，TTL 加随机值（防雪崩）
            stringRedisTemplate.opsForValue().set(
                    key,
                    objectMapper.writeValueAsString(goods),
                    RedisConstants.CACHE_GOODS_TTL + ThreadLocalRandom.current().nextInt(10),
                    TimeUnit.MINUTES
            );
            recordUv(id);
            return Result.ok(goods);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException(e);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            if (locked) {
                lock.unlock();
            }
        }
    }

    @Override
    public Result pageList(int page, int size, String category, String keyword, String sort,
                           BigDecimal priceMin, BigDecimal priceMax, String condition) {
        Page<Goods> p = new Page<>(page, size);
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getStatus, 1);
        if (StringUtils.hasText(category)) {
            wrapper.eq(Goods::getCategory, category);
        }
        if (StringUtils.hasText(condition)) {
            wrapper.eq(Goods::getConditionDesc, condition);
        }
        if (priceMin != null) {
            wrapper.ge(Goods::getPrice, priceMin);
        }
        if (priceMax != null) {
            wrapper.le(Goods::getPrice, priceMax);
        }
        if (StringUtils.hasText(keyword)) {
            // 记录搜索历史 + 热门搜索词
            Long userId = UserHolder.getUserId();
            if (userId != null) {
                stringRedisTemplate.opsForZSet().add(
                        RedisConstants.SEARCH_HISTORY_KEY + userId, keyword, System.currentTimeMillis());
            }
            stringRedisTemplate.opsForZSet().incrementScore(RedisConstants.SEARCH_HOT_KEY, keyword, 1);
            wrapper.and(w -> w.like(Goods::getTitle, keyword).or().like(Goods::getDescription, keyword));
        }
        if ("price".equals(sort)) {
            wrapper.orderByAsc(Goods::getPrice);
        } else if ("price_desc".equals(sort)) {
            wrapper.orderByDesc(Goods::getPrice);
        } else {
            wrapper.orderByDesc(Goods::getCreateTime);
        }
        page(p, wrapper);
        return Result.ok(p.getRecords(), p.getTotal());
    }

    @Override
    public Result publish(Goods goods) {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.fail("未登录");
        }
        goods.setSellerId(userId);
        goods.setStatus(1);
        goods.setViewCount(0);
        goods.setStock(goods.getStock() == null ? 1 : goods.getStock());
        goods.setIsHot(goods.getIsHot() == null ? 0 : goods.getIsHot());
        goods.setCreateTime(LocalDateTime.now());
        goods.setUpdateTime(LocalDateTime.now());
        save(goods);

        // 地理位置索引（附近商品）
        if (goods.getLongitude() != null && goods.getLatitude() != null) {
            stringRedisTemplate.opsForGeo().add(
                    GEO_KEY, new Point(goods.getLongitude(), goods.getLatitude()), goods.getId().toString());
        }

        // Feed 推送给粉丝（推模式，异步线程池执行）
        feedPushService.pushToFans(userId, goods.getId());
        return Result.ok(goods.getId());
    }

    @Override
    public Result updateGoods(Goods goods) {
        Long userId = UserHolder.getUserId();
        Goods db = baseMapper.selectById(goods.getId());
        if (db == null || !db.getSellerId().equals(userId)) {
            return Result.fail("无权操作该商品");
        }
        goods.setUpdateTime(LocalDateTime.now());
        updateById(goods);
        stringRedisTemplate.delete(RedisConstants.CACHE_GOODS_KEY + goods.getId());
        return Result.ok();
    }

    @Override
    public Result deleteGoods(Long id) {
        Long userId = UserHolder.getUserId();
        Goods db = baseMapper.selectById(id);
        if (db == null || !db.getSellerId().equals(userId)) {
            return Result.fail("无权操作该商品");
        }
        removeById(id);
        stringRedisTemplate.delete(RedisConstants.CACHE_GOODS_KEY + id);
        return Result.ok();
    }

    @Override
    public Result hotList() {
        LambdaQueryWrapper<Goods> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Goods::getIsHot, 1).eq(Goods::getStatus, 1).orderByDesc(Goods::getCreateTime);
        return Result.ok(list(wrapper));
    }

    @Override
    public Result nearby(Double longitude, Double latitude, Double radius) {
        Circle circle = new Circle(new Point(longitude, latitude), new Distance(radius, Metrics.KILOMETERS));
        GeoResults<RedisGeoCommands.GeoLocation<String>> results =
                stringRedisTemplate.opsForGeo().radius(GEO_KEY, circle);
        List<Goods> goodsList = new ArrayList<>();
        if (results != null) {
            for (GeoResult<RedisGeoCommands.GeoLocation<String>> r : results) {
                String goodsId = r.getContent().getName();
                Goods goods = baseMapper.selectById(Long.valueOf(goodsId));
                if (goods != null && Integer.valueOf(1).equals(goods.getStatus())) {
                    goodsList.add(goods);
                }
            }
        }
        return Result.ok(goodsList);
    }

    @Override
    public Result uvCount(Long id) {
        Long uv = stringRedisTemplate.opsForHyperLogLog().size(RedisConstants.GOODS_UV_KEY + id);
        return Result.ok(uv);
    }

    @Override
    public Result searchHistory() {
        Long userId = UserHolder.getUserId();
        if (userId == null) {
            return Result.ok(List.of());
        }
        Set<String> history = stringRedisTemplate.opsForZSet().reverseRange(
                RedisConstants.SEARCH_HISTORY_KEY + userId, 0, 9);
        return Result.ok(history == null ? List.of() : new ArrayList<>(history));
    }

    @Override
    public Result searchHot() {
        Set<String> hot = stringRedisTemplate.opsForZSet().reverseRange(RedisConstants.SEARCH_HOT_KEY, 0, 9);
        return Result.ok(hot == null ? List.of() : new ArrayList<>(hot));
    }

    /** 记录 UV（HyperLogLog） */
    private void recordUv(Long goodsId) {
        Long userId = UserHolder.getUserId();
        if (userId != null) {
            stringRedisTemplate.opsForHyperLogLog().add(RedisConstants.GOODS_UV_KEY + goodsId, userId.toString());
        }
    }
}
