package com.nuaa.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Order;

public interface OrderService extends IService<Order> {

    /** 普通下单 */
    Result createOrder(Long goodsId);

    /** 秒杀下单（Lua + 分布式锁 + RabbitMQ 异步） */
    Result seckill(Long goodsId);

    /** 我的订单（买家） */
    Result myOrders();

    /** 更新订单状态（买家/卖家） */
    Result updateStatus(Long orderId, Integer status);

    /** 异步创建订单（RabbitMQ 消费者调用） */
    void createOrderAsync(Long goodsId, Long userId);
}
