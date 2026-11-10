package com.nuaa.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Favorite;

public interface FavoriteService extends IService<Favorite> {

    /** 收藏 */
    Result add(Long goodsId);

    /** 取消收藏 */
    Result remove(Long goodsId);

    /** 是否已收藏 */
    Result isFavorite(Long goodsId);

    /** 我的收藏列表 */
    Result myList();
}
