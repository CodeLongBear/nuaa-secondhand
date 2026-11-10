package com.nuaa.secondhand.mq;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nuaa.secondhand.config.RabbitConfig;
import com.nuaa.secondhand.entity.Message;
import com.nuaa.secondhand.mapper.MessageMapper;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/** 站内消息消费者：MQ 异步落库 */
@Slf4j
@Component
public class MessageListener {

    @Resource
    private MessageMapper messageMapper;

    @Resource
    private ObjectMapper objectMapper;

    @RabbitListener(queues = RabbitConfig.NOTIFY_QUEUE)
    public void onMessage(String json) {
        try {
            JsonNode node = objectMapper.readTree(json);
            Message message = new Message();
            message.setFromUserId(node.path("fromUserId").asLong());
            message.setToUserId(node.path("toUserId").asLong());
            message.setType(node.path("type").asInt());
            message.setContent(node.path("content").asText());
            if (node.hasNonNull("goodsId")) {
                message.setGoodsId(node.path("goodsId").asLong());
            }
            message.setIsRead(0);
            message.setCreateTime(LocalDateTime.now());
            messageMapper.insert(message);
        } catch (Exception e) {
            log.error("处理站内通知消息失败：{}", json, e);
        }
    }
}
