package com.nuaa.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 商品评论/留言（支持回复） */
@Data
@TableName("tb_comment")
public class Comment {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 商品 id */
    private Long goodsId;

    /** 评论人 */
    private Long userId;

    /** 父评论 id，0 表示顶层评论 */
    private Long parentId;

    private String content;

    private LocalDateTime createTime;
}
