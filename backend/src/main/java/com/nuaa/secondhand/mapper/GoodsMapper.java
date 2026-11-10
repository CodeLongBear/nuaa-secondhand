package com.nuaa.secondhand.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.nuaa.secondhand.entity.Goods;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface GoodsMapper extends BaseMapper<Goods> {

    /** 原子扣库存，卖完自动下架（防数据库层超卖） */
    @Update("UPDATE tb_goods SET stock = stock - 1, status = IF(stock - 1 <= 0, 2, status) WHERE id = #{id} AND stock > 0")
    int deductStock(@Param("id") Long id);

    /** 回补库存（取消订单用） */
    @Update("UPDATE tb_goods SET stock = stock + 1 WHERE id = #{id}")
    int addStock(@Param("id") Long id);
}
