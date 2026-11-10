package com.nuaa.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 订单评价（交易完成后互评） */
@Data
@TableName("tb_review")
public class Review {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关联订单 */
    private Long orderId;

    /** 评价人 */
    private Long reviewerId;

    /** 被评价人 */
    private Long targetUserId;

    /** 评分 1-5 */
    private Integer score;

    private String content;

    private LocalDateTime createTime;
}
