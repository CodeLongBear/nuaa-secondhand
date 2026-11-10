package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Comment;
import com.nuaa.secondhand.entity.Goods;
import com.nuaa.secondhand.entity.User;
import com.nuaa.secondhand.mapper.CommentMapper;
import com.nuaa.secondhand.mapper.GoodsMapper;
import com.nuaa.secondhand.mapper.UserMapper;
import com.nuaa.secondhand.service.CommentService;
import com.nuaa.secondhand.service.MessageService;
import com.nuaa.secondhand.utils.UserHolder;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** 评论服务：商品留言 + 回复 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements CommentService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private MessageService messageService;

    @Override
    public Result listByGoods(Long goodsId) {
        List<Comment> comments = this.list(new LambdaQueryWrapper<Comment>()
                .eq(Comment::getGoodsId, goodsId)
                .orderByAsc(Comment::getCreateTime));
        List<Map<String, Object>> result = new ArrayList<>();
        for (Comment c : comments) {
            User u = userMapper.selectById(c.getUserId());
            Map<String, Object> m = new HashMap<>();
            m.put("id", c.getId());
            m.put("goodsId", c.getGoodsId());
            m.put("userId", c.getUserId());
            m.put("nickname", u == null ? "用户" + c.getUserId() : u.getNickname());
            m.put("parentId", c.getParentId());
            m.put("content", c.getContent());
            m.put("createTime", c.getCreateTime());
            result.add(m);
        }
        return Result.ok(result);
    }

    @Override
    public Result add(Long goodsId, Long parentId, String content) {
        Long userId = UserHolder.getUserId();
        if (content == null || content.trim().isEmpty()) {
            return Result.fail("评论内容不能为空");
        }
        Comment comment = new Comment();
        comment.setGoodsId(goodsId);
        comment.setUserId(userId);
        comment.setParentId(parentId == null ? 0L : parentId);
        comment.setContent(content);
        comment.setCreateTime(LocalDateTime.now());
        save(comment);
        Goods goods = goodsMapper.selectById(goodsId);
        if (goods != null) {
            messageService.notify(userId, goods.getSellerId(), 2, "你的商品收到一条新评论", goodsId);
        }
        return Result.ok(comment.getId());
    }
}
