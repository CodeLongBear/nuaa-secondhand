package com.nuaa.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Review;

public interface ReviewService extends IService<Review> {

    /** 交易完成后评价（回写信誉积分） */
    Result create(Long orderId, Integer score, String content);

    /** 某用户收到的评价列表 */
    Result listByUser(Long userId);

    /** 某订单的评价列表 */
    Result listByOrder(Long orderId);
}
