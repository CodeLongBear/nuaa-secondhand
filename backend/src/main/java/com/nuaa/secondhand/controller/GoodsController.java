package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Goods;
import com.nuaa.secondhand.service.GoodsService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/goods")
public class GoodsController {

    @Resource
    private GoodsService goodsService;

    /** 商品详情 */
    @GetMapping("/detail/{id}")
    public Result detail(@PathVariable Long id) {
        return goodsService.detail(id);
    }

    /** 商品列表 / 搜索 */
    @GetMapping("/list")
    public Result list(@RequestParam(defaultValue = "1") int page,
                       @RequestParam(defaultValue = "10") int size,
                       @RequestParam(required = false) String category,
                       @RequestParam(required = false) String keyword,
                       @RequestParam(required = false) String sort) {
        return goodsService.pageList(page, size, category, keyword, sort);
    }

    /** 发布商品 */
    @PostMapping("/publish")
    public Result publish(@RequestBody Goods goods) {
        return goodsService.publish(goods);
    }

    /** 更新商品 */
    @PutMapping
    public Result update(@RequestBody Goods goods) {
        return goodsService.updateGoods(goods);
    }

    /** 下架/删除 */
    @DeleteMapping("/{id}")
    public Result delete(@PathVariable Long id) {
        return goodsService.deleteGoods(id);
    }

    /** 热门抢购 */
    @GetMapping("/hot")
    public Result hot() {
        return goodsService.hotList();
    }

    /** 附近的商品（GeoHash） */
    @GetMapping("/nearby")
    public Result nearby(@RequestParam Double longitude,
                         @RequestParam Double latitude,
                         @RequestParam(defaultValue = "5") Double radius) {
        return goodsService.nearby(longitude, latitude, radius);
    }

    /** 商品 UV 浏览量 */
    @GetMapping("/uv/{id}")
    public Result uv(@PathVariable Long id) {
        return goodsService.uvCount(id);
    }
}
