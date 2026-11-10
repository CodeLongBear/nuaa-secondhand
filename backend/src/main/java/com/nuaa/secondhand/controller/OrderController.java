package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.service.OrderService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/order")
public class OrderController {

    @Resource
    private OrderService orderService;

    /** 普通下单 */
    @PostMapping("/{goodsId}")
    public Result create(@PathVariable Long goodsId) {
        return orderService.createOrder(goodsId);
    }

    /** 秒杀下单 */
    @PostMapping("/seckill/{goodsId}")
    public Result seckill(@PathVariable Long goodsId) {
        return orderService.seckill(goodsId);
    }

    /** 我的订单 */
    @GetMapping("/my")
    public Result my() {
        return orderService.myOrders();
    }

    /** 更新订单状态 */
    @PutMapping("/{orderId}/{status}")
    public Result updateStatus(@PathVariable Long orderId, @PathVariable Integer status) {
        return orderService.updateStatus(orderId, status);
    }
}
