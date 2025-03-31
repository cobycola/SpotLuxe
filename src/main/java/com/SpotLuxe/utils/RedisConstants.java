package com.SpotLuxe.utils;

public class RedisConstants {
    // 登录验证码的常量
    public static final String LOGIN_CODE_KEY = "login:code:";
    public static final Long LOGIN_CODE_TTL = 2L;

    // 登录用户的token常量
    public static final String LOGIN_USER_KEY = "login:token:";
    public static final Long LOGIN_USER_TTL = 36000L;

    // 缓存空值的常量
    public static final Long CACHE_NULL_TTL = 2L;

    // 缓存商铺信息的常量
    public static final Long CACHE_SHOP_TTL = 30L;
    public static final String CACHE_SHOP_KEY = "cache:shop:";

    // 缓存商铺类型的常量
    public static final Long CACHE_SHOP_TYPE_TTL = 30L;
    public static final String CACHE_SHOP_TYPE_KEY = "cache:shopType";

    // 缓存商铺信息的常量
    public static final String LOCK_SHOP_KEY = "lock:shop:";
    public static final Long LOCK_SHOP_TTL = 10L;


    // 缓存秒杀库存的常量
    public static final String SECKILL_STOCK_KEY = "seckill:stock:";
}
