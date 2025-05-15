package com.terra.framework.geyser.util;

import com.terra.framework.geyser.factory.CacheFactory;
import com.terra.framework.geyser.options.CacheOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存工具 <br/>
 *
 * @author Leon Sun
 * @since 2022/4/28
 */
@Slf4j
public class GuavaCacheUtils {


    private static CacheFactory cacheFactory;


    public GuavaCacheUtils(CacheFactory cacheFactory) {
        GuavaCacheUtils.cacheFactory = cacheFactory;
    }


    /**
     * 创建或获取一个访问后过期的缓存
     */
    public static <K, V> CacheOperation<K, V> createAccessCache(String name, int maxSize, long expireAfterAccess, TimeUnit timeUnit) {
        return cacheFactory.createAccessCache(name, maxSize, expireAfterAccess, timeUnit);
    }

    /**
     * 创建或获取一个写入后过期的缓存
     */
    public static <K, V> CacheOperation<K, V> createWriteCache(String name, int maxSize, long expireAfterWrite, TimeUnit timeUnit) {
        return cacheFactory.createWriteCache(name, maxSize, expireAfterWrite, timeUnit);
    }

    /**
     * 获取指定名称的缓存
     */
    public static <K, V> CacheOperation<K, V> getCache(String name) {
        return cacheFactory.getCache(name);
    }

}
