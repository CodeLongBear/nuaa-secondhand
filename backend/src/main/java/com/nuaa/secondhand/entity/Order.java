package com.nuaa.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 订单 */
@Data
@TableName("tb_order")
public class Order {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 订单号 */
    private String orderNo;

    private Long goodsId;

    /** 买家 */
    private Long buyerId;

    /** 卖家 */
    private Long sellerId;

    private BigDecimal price;

    /** 状态：1 已拍 2 面交中 3 已完成 4 已取消 */
    private Integer status;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;
}
