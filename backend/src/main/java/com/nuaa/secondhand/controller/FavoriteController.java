package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.service.FavoriteService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/favorite")
public class FavoriteController {

    @Resource
    private FavoriteService favoriteService;

    /** 收藏 */
    @PostMapping("/{goodsId}")
    public Result add(@PathVariable Long goodsId) {
        return favoriteService.add(goodsId);
    }

    /** 取消收藏 */
    @DeleteMapping("/{goodsId}")
    public Result remove(@PathVariable Long goodsId) {
        return favoriteService.remove(goodsId);
    }

    /** 是否已收藏 */
    @GetMapping("/isFavorite/{goodsId}")
    public Result isFavorite(@PathVariable Long goodsId) {
        return favoriteService.isFavorite(goodsId);
    }

    /** 我的收藏 */
    @GetMapping("/my")
    public Result my() {
        return favoriteService.myList();
    }
}
