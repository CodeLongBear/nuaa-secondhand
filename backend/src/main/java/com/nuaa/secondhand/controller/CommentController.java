package com.nuaa.secondhand.controller;

import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.service.CommentService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/comment")
public class CommentController {

    @Resource
    private CommentService commentService;

    /** 某商品的评论列表 */
    @GetMapping("/goods/{goodsId}")
    public Result list(@PathVariable Long goodsId) {
        return commentService.listByGoods(goodsId);
    }

    /** 发表评论/回复 */
    @PostMapping
    public Result add(@RequestParam Long goodsId,
                      @RequestParam(required = false) Long parentId,
                      @RequestParam String content) {
        return commentService.add(goodsId, parentId, content);
    }
}
