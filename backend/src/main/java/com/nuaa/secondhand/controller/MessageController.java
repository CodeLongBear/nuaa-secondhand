package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.service.MessageService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/message")
public class MessageController {

    @Resource
    private MessageService messageService;

    /** 消息列表 */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "0") Integer onlyUnread) {
        return messageService.list(onlyUnread);
    }

    /** 未读数 */
    @GetMapping("/unreadCount")
    public Result unreadCount() {
        return messageService.unreadCount();
    }

    /** 标记已读 */
    @PutMapping("/read/{id}")
    public Result read(@PathVariable Long id) {
        return messageService.markRead(id);
    }
}
