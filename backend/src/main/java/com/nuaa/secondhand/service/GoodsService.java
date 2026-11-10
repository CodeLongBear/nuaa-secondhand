package com.nuaa.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Goods;

public interface GoodsService extends IService<Goods> {

    /** 商品详情（带 Redis 缓存） */
    Result detail(Long id);

    /** 分页列表 / 搜索 / 附近 */
    Result pageList(int page, int size, String category, String keyword, String sort);

    /** 发布商品 */
    Result publish(Goods goods);

    /** 更新商品（删缓存） */
    Result updateGoods(Goods goods);

    /** 下架/删除（删缓存） */
    Result deleteGoods(Long id);

    /** 热门抢购商品列表 */
    Result hotList();

    /** 附近的商品（GeoHash） */
    Result nearby(Double longitude, Double latitude, Double radius);

    /** 商品 UV 浏览量（HyperLogLog） */
    Result uvCount(Long id);
}
