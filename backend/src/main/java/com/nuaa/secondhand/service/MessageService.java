package com.nuaa.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Message;

public interface MessageService extends IService<Message> {

    /** 发送站内通知（走 MQ 异步落库） */
    void notify(Long fromUserId, Long toUserId, Integer type, String content, Long goodsId);

    /** 我的消息列表（onlyUnread=1 只看未读） */
    Result list(Integer onlyUnread);

    /** 标记已读 */
    Result markRead(Long messageId);

    /** 未读数 */
    Result unreadCount();
}
