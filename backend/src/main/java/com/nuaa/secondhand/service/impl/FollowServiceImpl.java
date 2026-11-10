package com.nuaa.secondhand.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.nuaa.secondhand.dto.Result;
import com.nuaa.secondhand.entity.Follow;
import com.nuaa.secondhand.entity.Goods;
import com.nuaa.secondhand.mapper.FollowMapper;
import com.nuaa.secondhand.mapper.GoodsMapper;
import com.nuaa.secondhand.service.FollowService;
import com.nuaa.secondhand.service.MessageService;
import com.nuaa.secondhand.utils.RedisConstants;
import com.nuaa.secondhand.utils.UserHolder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** 关注服务：关注/粉丝/共同关注 + Feed 流（推模式） */
@Slf4j
@Service
public class FollowServiceImpl extends ServiceImpl<FollowMapper, Follow> implements FollowService {

    private static final String FANS_KEY = "fans:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private GoodsMapper goodsMapper;

    @Resource
    private MessageService messageService;

    @Override
    public Result follow(Long followUserId, Boolean isFollow) {
        Long userId = UserHolder.getUserId();
        String followKey = RedisConstants.FOLLOW_KEY + userId;
        String fansKey = FANS_KEY + followUserId;
        if (Boolean.TRUE.equals(isFollow)) {
            // 落库（幂等：已存在则跳过，避免唯一键冲突）
            long exist = this.count(new LambdaQueryWrapper<Follow>()
                    .eq(Follow::getUserId, userId)
                    .eq(Follow::getFollowUserId, followUserId));
            if (exist == 0) {
                Follow follow = new Follow();
                follow.setUserId(userId);
                follow.setFollowUserId(followUserId);
                follow.setCreateTime(LocalDateTime.now());
                save(follow);
            }
            stringRedisTemplate.opsForSet().add(followKey, followUserId.toString());
            stringRedisTemplate.opsForSet().add(fansKey, userId.toString());
            messageService.notify(userId, followUserId, 3, "有用户关注了你", null);
        } else {
            remove(new LambdaQueryWrapper<Follow>()
                    .eq(Follow::getUserId, userId)
                    .eq(Follow::getFollowUserId, followUserId));
            stringRedisTemplate.opsForSet().remove(followKey, followUserId.toString());
            stringRedisTemplate.opsForSet().remove(fansKey, userId.toString());
        }
        return Result.ok();
    }

    @Override
    public Result isFollow(Long followUserId) {
        Long userId = UserHolder.getUserId();
        Boolean isFollow = stringRedisTemplate.opsForSet().isMember(
                RedisConstants.FOLLOW_KEY + userId, followUserId.toString());
        return Result.ok(isFollow);
    }

    @Override
    public Result commonFollow(Long otherId) {
        Long userId = UserHolder.getUserId();
        Set<String> common = stringRedisTemplate.opsForSet().intersect(
                RedisConstants.FOLLOW_KEY + userId, RedisConstants.FOLLOW_KEY + otherId);
        return Result.ok(common);
    }

    @Override
    public Result feed() {
        Long userId = UserHolder.getUserId();
        Set<String> goodsIds = stringRedisTemplate.opsForZSet().reverseRange(
                RedisConstants.FEED_KEY + userId, 0, 19);
        List<Goods> goodsList = new ArrayList<>();
        if (goodsIds != null) {
            for (String id : goodsIds) {
                Goods goods = goodsMapper.selectById(Long.valueOf(id));
                if (goods != null && Integer.valueOf(1).equals(goods.getStatus())) {
                    goodsList.add(goods);
                }
            }
        }
        return Result.ok(goodsList);
    }

    @Override
    public Result myFollows() {
        Long userId = UserHolder.getUserId();
        Set<String> follows = stringRedisTemplate.opsForSet().members(RedisConstants.FOLLOW_KEY + userId);
        if (follows == null || follows.isEmpty()) {
            return Result.ok(List.of());
        }
        List<Long> ids = follows.stream().map(Long::valueOf).toList();
        return Result.ok(ids);
    }
}
