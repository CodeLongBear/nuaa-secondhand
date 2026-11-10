package com.nuaa.secondhand.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Comment;

public interface CommentService extends IService<Comment> {

    /** 某商品的评论列表（含回复，附带昵称） */
    Result listByGoods(Long goodsId);

    /** 发表评论/回复 */
    Result add(Long goodsId, Long parentId, String content);
}
