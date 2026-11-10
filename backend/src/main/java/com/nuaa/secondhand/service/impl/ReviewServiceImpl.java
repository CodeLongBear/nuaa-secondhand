package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Order;
import com.nuaa.secondhand.entity.Review;
import com.nuaa.secondhand.entity.User;
import com.nuaa.secondhand.mapper.OrderMapper;
import com.nuaa.secondhand.mapper.ReviewMapper;
import com.nuaa.secondhand.mapper.UserMapper;
import com.nuaa.secondhand.service.MessageService;
import com.nuaa.secondhand.service.ReviewService;
import com.nuaa.secondhand.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/** 评价服务：交易完成后互评 + 信誉积分联动 */
@Service
public class ReviewServiceImpl extends ServiceImpl<ReviewMapper, Review> implements ReviewService {

    @Resource
    private OrderMapper orderMapper;

    @Resource
    private UserMapper userMapper;

    @Resource
    private MessageService messageService;

    @Override
    @Transactional
    public Result create(Long orderId, Integer score, String content) {
        Long userId = UserHolder.getUserId();
        Order order = orderMapper.selectById(orderId);
        if (order == null) {
            return Result.fail("订单不存在");
        }
        boolean isBuyer = order.getBuyerId().equals(userId);
        boolean isSeller = order.getSellerId().equals(userId);
        if (!isBuyer && !isSeller) {
            return Result.fail("无权评价该订单");
        }
        if (!Integer.valueOf(3).equals(order.getStatus())) {
            return Result.fail("订单完成后才能评价");
        }
        if (score == null || score < 1 || score > 5) {
            return Result.fail("评分需在 1-5 之间");
        }
        // 一人一单一评
        long exist = this.count(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId)
                .eq(Review::getReviewerId, userId));
        if (exist > 0) {
            return Result.fail("已评价过该订单");
        }

        // 被评人为对方
        Long targetUserId = isBuyer ? order.getSellerId() : order.getBuyerId();
        Review review = new Review();
        review.setOrderId(orderId);
        review.setReviewerId(userId);
        review.setTargetUserId(targetUserId);
        review.setScore(score);
        review.setContent(content);
        review.setCreateTime(LocalDateTime.now());
        save(review);

        // 回写信誉积分
        User target = userMapper.selectById(targetUserId);
        if (target != null) {
            int credit = target.getCredit() == null ? 0 : target.getCredit();
            target.setCredit(credit + score);
            userMapper.updateById(target);
        }
        messageService.notify(userId, targetUserId, 4, "你收到一条新评价", null);
        return Result.ok();
    }

    @Override
    public Result listByUser(Long userId) {
        return Result.ok(this.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getTargetUserId, userId)
                .orderByDesc(Review::getCreateTime)));
    }

    @Override
    public Result listByOrder(Long orderId) {
        return Result.ok(this.list(new LambdaQueryWrapper<Review>()
                .eq(Review::getOrderId, orderId)
                .orderByDesc(Review::getCreateTime)));
    }
}
