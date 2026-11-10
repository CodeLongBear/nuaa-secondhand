package com.nuaa.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.time.LocalDateTime;

/** 收藏 / 心愿单 */
@Data
@TableName("tb_favorite")
public class Favorite {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private Long userId;

    private Long goodsId;

    private LocalDateTime createTime;
}
