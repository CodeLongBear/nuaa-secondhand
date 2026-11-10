package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuaa.secondhand.config.RabbitConfig;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Goods;
import com.nuaa.secondhand.entity.Order;
import com.nuaa.secondhand.mapper.GoodsMapper;
import com.nuaa.secondhand.mapper.OrderMapper;
import com.nuaa.secondhand.service.OrderService;
import com.nuaa.secondhand.utils.RedisConstants;
import com.nuaa.secondhand.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

/** 订单服务：普通下单 + 秒杀（Lua 原子扣减 + RabbitMQ 异步） */
@Slf4j
@Service
public class OrderServiceImpl extends ServiceImpl<OrderMapper, Order> implements OrderService {

    private static final DefaultRedisScript<Long> SECKILL_SCRIPT;

    static {
        SECKILL_SCRIPT = new DefaultRedisScript<>();
        SECKILL_SCRIPT.setLocation(new ClassPathResource("lua/seckill.lua"));
        SECKILL_SCRIPT.setResultType(Long.class);
    }

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private GoodsMapper goodsMapper;

    @Override
    public Result createOrder(Long goodsId) {
        Long userId = UserHolder.getUserId();
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || !Integer.valueOf(1).equals(goods.getStatus())) {
            return Result.fail("商品不存在或已下架");
        }
        if (goods.getSellerId().equals(userId)) {
            return Result.fail("不能购买自己的商品");
        }
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setGoodsId(goodsId);
        order.setBuyerId(userId);
        order.setSellerId(goods.getSellerId());
        order.setPrice(goods.getPrice());
        order.setStatus(1);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);
        return Result.ok(order.getId());
    }

    @Override
    public Result seckill(Long goodsId) {
        Long userId = UserHolder.getUserId();
        String stockKey = RedisConstants.SECKILL_STOCK_KEY + goodsId;
        String orderKey = RedisConstants.SECKILL_ORDER_KEY + goodsId;

        // 懒加载库存到 Redis（幂等）
        String stock = stringRedisTemplate.opsForValue().get(stockKey);
        if (stock == null) {
            Goods goods = goodsMapper.selectById(goodsId);
            if (goods == null || !Integer.valueOf(1).equals(goods.getIsHot())) {
                return Result.fail("该商品不支持抢购");
            }
            stringRedisTemplate.opsForValue().set(stockKey, String.valueOf(goods.getStock()));
        }

        // Lua 原子：判断库存 + 去重 + 扣减
        Long result = stringRedisTemplate.execute(
                SECKILL_SCRIPT, List.of(stockKey, orderKey), String.valueOf(userId));
        if (result == null) {
            return Result.fail("系统繁忙，请稍后");
        }
        if (result == 1) {
            return Result.fail("手慢了，库存不足");
        }
        if (result == 2) {
            return Result.fail("请勿重复下单");
        }

        // 发消息异步下单（削峰）
        rabbitTemplate.convertAndSend(
                RabbitConfig.SECKILL_EXCHANGE, RabbitConfig.SECKILL_ROUTING_KEY, goodsId + ":" + userId);
        return Result.ok("抢购成功，订单生成中");
    }

    @Override
    public Result myOrders() {
        Long userId = UserHolder.getUserId();
        return Result.ok(query().eq("buyer_id", userId).orderByDesc("create_time").list());
    }

    @Override
    public Result updateStatus(Long orderId, Integer status) {
        Long userId = UserHolder.getUserId();
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        if (!order.getBuyerId().equals(userId) && !order.getSellerId().equals(userId)) {
            return Result.fail("无权操作该订单");
        }
        order.setStatus(status);
        order.setUpdateTime(LocalDateTime.now());
        updateById(order);
        return Result.ok();
    }

    @Override
    @Transactional
    public void createOrderAsync(Long goodsId, Long userId) {
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null) {
            log.warn("秒杀下单：商品不存在 goodsId={}", goodsId);
            return;
        }
        Order order = new Order();
        order.setOrderNo(generateOrderNo());
        order.setGoodsId(goodsId);
        order.setBuyerId(userId);
        order.setSellerId(goods.getSellerId());
        order.setPrice(goods.getPrice());
        order.setStatus(1);
        order.setCreateTime(LocalDateTime.now());
        order.setUpdateTime(LocalDateTime.now());
        save(order);

        // 扣减数据库库存，卖完下架
        int remain = goods.getStock() - 1;
        goods.setStock(remain);
        if (remain <= 0) {
            goods.setStatus(2);
        }
        goodsMapper.updateById(goods);
    }

    private String generateOrderNo() {
        return System.currentTimeMillis() + "" + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
