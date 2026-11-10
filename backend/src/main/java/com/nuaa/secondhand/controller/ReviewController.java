package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.service.ReviewService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/review")
public class ReviewController {

    @Resource
    private ReviewService reviewService;

    /** 创建评价 */
    @PostMapping
    public Result create(@RequestParam Long orderId,
                         @RequestParam Integer score,
                         @RequestParam(required = false) String content) {
        return reviewService.create(orderId, score, content);
    }

    /** 某用户收到的评价 */
    @GetMapping("/user/{userId}")
    public Result listByUser(@PathVariable Long userId) {
        return reviewService.listByUser(userId);
    }

    /** 某订单的评价 */
    @GetMapping("/order/{orderId}")
    public Result listByOrder(@PathVariable Long orderId) {
        return reviewService.listByOrder(orderId);
    }
}
