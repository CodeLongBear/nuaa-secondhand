package com.nuaa.secondhand.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableLogic;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/** 二手商品 */
@Data
@TableName("tb_goods")
public class Goods {

    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    private String title;

    private String description;

    /** 分类：数码/书籍/生活用品/服饰/运动/其他 */
    private String category;

    private BigDecimal price;

    /** 图片 URL，逗号分隔 */
    private String images;

    /** 状态：1 在售 2 已售 3 下架 */
    private Integer status;

    private Long sellerId;

    /** 经度 */
    private Double longitude;

    /** 纬度 */
    private Double latitude;

    /** 位置描述，如「将军路校区」 */
    private String location;

    /** 成色描述 */
    private String conditionDesc;

    /** 浏览量 */
    private Integer viewCount;

    /** 库存（抢购商品用） */
    private Integer stock;

    /** 是否热门抢购：0 否 1 是 */
    private Integer isHot;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    @TableLogic
    private Integer deleted;
}
