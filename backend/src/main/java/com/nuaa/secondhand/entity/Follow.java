package com.nuaa.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 关注关系 */
@Data
@TableName("tb_follow")
public class Follow {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /** 关注者 */
    private Long userId;

    /** 被关注者 */
    private Long followUserId;

    private LocalDateTime createTime;
}
