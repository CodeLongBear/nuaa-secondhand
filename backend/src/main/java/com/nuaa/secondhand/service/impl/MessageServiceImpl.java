package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuaa.secondhand.config.RabbitConfig;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Message;
import com.nuaa.secondhand.mapper.MessageMapper;
import com.nuaa.secondhand.service.MessageService;
import com.nuaa.secondhand.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

/** 站内消息服务：发送走 MQ，消费端落库（异步解耦） */
@Slf4j
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements MessageService {

    @Resource
    private RabbitTemplate rabbitTemplate;

    @Resource
    private ObjectMapper objectMapper;

    @Override
    public void notify(Long fromUserId, Long toUserId, Integer type, String content, Long goodsId) {
        if (toUserId == null || toUserId.equals(fromUserId)) {
            return;
        }
        try {
            Map<String, Object> msg = new HashMap<>();
            msg.put("fromUserId", fromUserId);
            msg.put("toUserId", toUserId);
            msg.put("type", type);
            msg.put("content", content);
            msg.put("goodsId", goodsId);
            rabbitTemplate.convertAndSend(RabbitConfig.NOTIFY_EXCHANGE, RabbitConfig.NOTIFY_ROUTING_KEY,
                    objectMapper.writeValueAsString(msg));
        } catch (Exception e) {
            log.error("发送站内通知失败", e);
        }
    }

    @Override
    public Result list(Integer onlyUnread) {
        Long userId = UserHolder.getUserId();
        LambdaQueryWrapper<Message> wrapper = new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId);
        if (Integer.valueOf(1).equals(onlyUnread)) {
            wrapper.eq(Message::getIsRead, 0);
        }
        wrapper.orderByDesc(Message::getCreateTime);
        return Result.ok(this.list(wrapper));
    }

    @Override
    public Result markRead(Long messageId) {
        Message message = getById(messageId);
        if (message != null && message.getToUserId().equals(UserHolder.getUserId())) {
            message.setIsRead(1);
            updateById(message);
        }
        return Result.ok();
    }

    @Override
    public Result unreadCount() {
        Long userId = UserHolder.getUserId();
        long count = this.count(new LambdaQueryWrapper<Message>()
                .eq(Message::getToUserId, userId)
                .eq(Message::getIsRead, 0));
        return Result.ok(count);
    }
}
