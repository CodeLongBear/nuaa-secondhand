package com.nuaa.secondhand.config;

import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/** RabbitMQ 配置：秒杀异步下单队列 */
@Configuration
public class RabbitConfig {

    public static final String SECKILL_QUEUE = "seckill.order.queue";
    public static final String SECKILL_EXCHANGE = "seckill.order.exchange";
    public static final String SECKILL_ROUTING_KEY = "seckill.order";

    public static final String NOTIFY_QUEUE = "notify.queue";
    public static final String NOTIFY_EXCHANGE = "notify.exchange";
    public static final String NOTIFY_ROUTING_KEY = "notify";

    @Bean
    public Queue seckillQueue() {
        return new Queue(SECKILL_QUEUE, true);
    }

    @Bean
    public DirectExchange seckillExchange() {
        return new DirectExchange(SECKILL_EXCHANGE);
    }

    @Bean
    public Binding seckillBinding() {
        return BindingBuilder.bind(seckillQueue()).to(seckillExchange()).with(SECKILL_ROUTING_KEY);
    }

    // ===== 站内消息通知 =====

    @Bean
    public Queue notifyQueue() {
        return new Queue(NOTIFY_QUEUE, true);
    }

    @Bean
    public DirectExchange notifyExchange() {
        return new DirectExchange(NOTIFY_EXCHANGE);
    }

    @Bean
    public Binding notifyBinding() {
        return BindingBuilder.bind(notifyQueue()).to(notifyExchange()).with(NOTIFY_ROUTING_KEY);
    }
}
