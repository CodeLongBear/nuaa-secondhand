package com.nuaa.secondhand.service;

import com.nuaa.secondhand.utils.RedisConstants;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.Set;

/** Feed 推送：发布商品时把动态推给粉丝（异步线程池执行，不阻塞发布主流程） */
@Service
public class FeedPushService {

    private static final String FANS_KEY = "fans:";

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Async("feedExecutor")
    public void pushToFans(Long sellerId, Long goodsId) {
        Set<String> fans = stringRedisTemplate.opsForSet().members(FANS_KEY + sellerId);
        if (fans != null && !fans.isEmpty()) {
            long score = System.currentTimeMillis();
            for (String fanId : fans) {
                stringRedisTemplate.opsForZSet().add(RedisConstants.FEED_KEY + fanId, goodsId.toString(), score);
            }
        }
    }
}
