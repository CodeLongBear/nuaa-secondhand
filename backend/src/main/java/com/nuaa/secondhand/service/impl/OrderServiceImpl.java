package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuaa.secondhand.config.RabbitConfig;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Goods;
import com.nuaa.secondhand.entity.Order;
import com.nuaa.secondhand.mapper.GoodsMapper;
import com.nuaa.secondhand.mapper.OrderMapper;
import com.nuaa.secondhand.service.MessageService;
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

    @Resource
    private MessageService messageService;

    @Override
    @Transactional
    public Result createOrder(Long goodsId) {
        Long userId = UserHolder.getUserId();
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods == null || !Integer.valueOf(1).equals(goods.getStatus())) {
            return Result.fail("商品不存在或已下架");
        }
        if (goods.getSellerId().equals(userId)) {
            return Result.fail("不能购买自己的商品");
        }
        // 原子扣库存（普通商品库存为 1，卖出即自动下架）
        if (goodsMapper.deductStock(goodsId) == 0) {
            return Result.fail("库存不足");
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
        messageService.notify(userId, goods.getSellerId(), 1, "有用户下单了你的商品", goodsId);
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
    @Transactional
    public Result updateStatus(Long orderId, Integer status) {
        Long userId = UserHolder.getUserId();
        Order order = getById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        boolean isBuyer = order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            return Result.fail("无权操作该订单");
        }

        int current = order.getStatus();
        // 终态不可再改
        if (current == 3 || current == 4) {
            return Result.fail("订单已结束，无法再修改状态");
        }

        // 状态机：1已拍 → 2面交中(卖家发货) / 4已取消；2面交中 → 3已完成(买家收货)
        if (current == 1 && status == 2) {
            if (!isSeller) {
                return Result.fail("仅卖家可确认发货");
            }
        } else if (current == 2 && status == 3) {
            if (!isBuyer) {
                return Result.fail("仅买家可确认收货");
            }
        } else if (current == 1 && status == 4) {
            // 取消订单：回补库存，商品若已售出下架则恢复在售
            Goods goods = goodsMapper.selectById(order.getGoodsId());
            if (goods != null) {
                goodsMapper.addStock(order.getGoodsId());
                if (Integer.valueOf(2).equals(goods.getStatus())) {
                    goods.setStatus(1);
                    goodsMapper.updateById(goods);
                }
            }
        } else {
            return Result.fail("非法的状态流转");
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

        // 原子扣库存 + 卖完自动下架（防数据库层超卖）
        int updated = goodsMapper.deductStock(goodsId);
        if (updated == 0) {
            log.warn("秒杀下单：数据库扣库存失败（库存不足或不一致）goodsId={}", goodsId);
        }
        messageService.notify(userId, goods.getSellerId(), 1, "有用户秒杀了你的商品", goodsId);
    }

    private String generateOrderNo() {
        return System.currentTimeMillis() + "" + ThreadLocalRandom.current().nextInt(1000, 9999);
    }
}
