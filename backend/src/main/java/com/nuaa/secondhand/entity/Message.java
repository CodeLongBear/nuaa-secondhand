package com.nuaa.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 站内消息通知 */
@Data
@TableName("tb_message")
public class Message {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 发送者，0 表示系统 */
    private Long fromUserId;

    /** 接收者 */
    private Long toUserId;

    /** 类型：1 下单 2 评论 3 关注 4 评价 */
    private Integer type;

    private String content;

    /** 关联商品 id */
    private Long goodsId;

    /** 是否已读：0 否 1 是 */
    private Integer isRead;

    private LocalDateTime createTime;
}
