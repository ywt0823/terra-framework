package com.terra.framework.strata.config.mysql.annonation;

import java.lang.annotation.*;
import java.util.concurrent.TimeUnit;

/**
 * 自动缓存注解
 * 用于手动标记需要缓存的方法
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AutoCache {

    /**
     * 缓存名称，默认使用方法全限定名
     */
    String name() default "";

    /**
     * 缓存键前缀
     */
    String keyPrefix() default "";

    /**
     * 本地缓存最大元素数量
     */
    int localMaxSize() default 1000;

    /**
     * 本地缓存过期时间
     */
    long localExpireTime() default 5;

    /**
     * Redis缓存过期时间
     */
    long redisExpireTime() default 30;

    /**
     * 时间单位
     */
    TimeUnit timeUnit() default TimeUnit.MINUTES;

    /**
     * 是否同步失效相关缓存
     * 当标记为true时，当关联的表数据被修改时，会自动清除相关缓存
     */
    boolean syncInvalidate() default true;

    /**
     * 相关表名
     * 用于关联缓存与数据表，以便在表数据变更时清除相关缓存
     */
    String[] tables() default {};
} 