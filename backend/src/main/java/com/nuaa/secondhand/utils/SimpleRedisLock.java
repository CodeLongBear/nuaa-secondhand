package com.nuaa.secondhand.utils;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * 基于 Redis 的简单分布式锁（setnx + 过期时间）。
 * value 用 UUID 标识，释放时用 Lua 校验，防止误删他人锁。
 */
public class SimpleRedisLock {

    private final StringRedisTemplate template;
    private final String key;
    private final String value;

    public SimpleRedisLock(StringRedisTemplate template, String key) {
        this.template = template;
        this.key = key;
        this.value = "lock:" + UUID.randomUUID().toString().replace("-", "");
    }

    /** 尝试加锁，成功返回 true */
    public boolean tryLock(long timeoutSec) {
        return Boolean.TRUE.equals(
                template.opsForValue().setIfAbsent(key, value, timeoutSec, TimeUnit.SECONDS)
        );
    }

    /** 释放锁（Lua 校验 value，避免误删） */
    public void unlock() {
        String script = "if redis.call('get', KEYS[1]) == ARGV[1] then return redis.call('del', KEYS[1]) else return 0 end";
        template.execute(new DefaultRedisScript<>(script, Long.class), List.of(key), value);
    }
}
