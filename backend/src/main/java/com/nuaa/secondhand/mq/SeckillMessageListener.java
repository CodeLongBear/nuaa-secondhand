package com.nuaa.secondhand.mq;

import com.nuaa.secondhand.config.RabbitConfig;
import com.nuaa.secondhand.service.OrderService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

/** 秒杀下单消息消费者：异步创建订单 */
@Slf4j
@Component
public class SeckillMessageListener {

    @Resource
    private OrderService orderService;

    @RabbitListener(queues = RabbitConfig.SECKILL_QUEUE)
    public void onSeckillOrder(String message) {
        log.info("收到秒杀下单消息: {}", message);
        try {
            String[] parts = message.split(":");
            Long goodsId = Long.valueOf(parts[0]);
            Long userId = Long.valueOf(parts[1]);
            orderService.createOrderAsync(goodsId, userId);
        } catch (Exception e) {
            log.error("秒杀下单处理失败: {}", message, e);
        }
    }
}
