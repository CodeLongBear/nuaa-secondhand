package com.nuaa.secondhand.utils;

/** Redis key 常量 */
public class RedisConstants {

    public static final String LOGIN_CODE_KEY = "login:code:";      // 验证码
    public static final Long LOGIN_CODE_TTL = 2L;                    // 验证码有效期（分钟）
    public static final String LOGIN_USER_KEY = "login:token:";      // 登录 token -> 用户 Hash
    public static final Long LOGIN_USER_TTL = 30L;                   // token 有效期（分钟）

    public static final String CACHE_GOODS_KEY = "cache:goods:";     // 商品详情缓存
    public static final Long CACHE_GOODS_TTL = 30L;                  // 缓存 TTL（分钟）
    public static final Long CACHE_NULL_TTL = 2L;                    // 空值缓存 TTL（分钟）

    public static final String SECKILL_STOCK_KEY = "seckill:stock:"; // 秒杀库存
    public static final String SECKILL_ORDER_KEY = "seckill:order:"; // 秒杀下单标记

    public static final String FEED_KEY = "feed:";                   // 关注动态 Feed（收件箱）
    public static final String FOLLOW_KEY = "follow:";               // 关注集合
    public static final String SIGN_KEY = "sign:";                   // 签到 BitMap
    public static final String GOODS_UV_KEY = "uv:goods:";           // 商品 UV（HLL）

    public static final String LOCK_KEY = "lock:";                   // 分布式锁前缀

    public static final String SEARCH_HISTORY_KEY = "search:history:"; // 用户搜索历史（ZSet）
    public static final String SEARCH_HOT_KEY = "search:hot";          // 热门搜索词（ZSet）
}
