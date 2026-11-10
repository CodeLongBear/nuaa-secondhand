package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Favorite;
import com.nuaa.secondhand.entity.Goods;
import com.nuaa.secondhand.mapper.FavoriteMapper;
import com.nuaa.secondhand.mapper.GoodsMapper;
import com.nuaa.secondhand.service.FavoriteService;
import com.nuaa.secondhand.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/** 收藏服务 */
@Service
public class FavoriteServiceImpl extends ServiceImpl<FavoriteMapper, Favorite> implements FavoriteService {

    @Resource
    private GoodsMapper goodsMapper;

    @Override
    public Result add(Long goodsId) {
        Long userId = UserHolder.getUserId();
        long exist = this.count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getGoodsId, goodsId));
        if (exist > 0) {
            return Result.fail("已收藏");
        }
        Favorite favorite = new Favorite();
        favorite.setUserId(userId);
        favorite.setGoodsId(goodsId);
        favorite.setCreateTime(LocalDateTime.now());
        save(favorite);
        return Result.ok();
    }

    @Override
    public Result remove(Long goodsId) {
        Long userId = UserHolder.getUserId();
        remove(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getGoodsId, goodsId));
        return Result.ok();
    }

    @Override
    public Result isFavorite(Long goodsId) {
        Long userId = UserHolder.getUserId();
        long count = this.count(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .eq(Favorite::getGoodsId, goodsId));
        return Result.ok(count > 0);
    }

    @Override
    public Result myList() {
        Long userId = UserHolder.getUserId();
        List<Favorite> favorites = this.list(new LambdaQueryWrapper<Favorite>()
                .eq(Favorite::getUserId, userId)
                .orderByDesc(Favorite::getCreateTime));
        List<Goods> goodsList = new ArrayList<>();
        for (Favorite f : favorites) {
            Goods goods = goodsMapper.selectById(f.getGoodsId());
            if (goods != null) {
                goodsList.add(goods);
            }
        }
        return Result.ok(goodsList);
    }
}
