package com.terra.framework.geyser.factory;

import com.terra.framework.geyser.options.CacheOperation;

import java.util.concurrent.TimeUnit;

/**
 * 缓存工厂接口
 * 负责创建和管理不同类型的缓存
 *
 * @author terra
 */
public interface CacheFactory {

    /**
     * 创建一个缓存
     *
     * @param name              缓存名称
     * @param maxSize           最大容量
     * @param expireAfterAccess 访问后过期时间
     * @param timeUnit          时间单位
     * @return 缓存操作对象
     */
    <K, V> CacheOperation<K, V> createAccessCache(String name, int maxSize, long expireAfterAccess, TimeUnit timeUnit);

    /**
     * 创建一个缓存
     *
     * @param name             缓存名称
     * @param maxSize          最大容量
     * @param expireAfterWrite 写入后过期时间
     * @param timeUnit         时间单位
     * @return 缓存操作对象
     */
    <K, V> CacheOperation<K, V> createWriteCache(String name, int maxSize, long expireAfterWrite, TimeUnit timeUnit);

    /**
     * 获取一个已创建的缓存
     *
     * @param name 缓存名称
     * @return 缓存操作对象
     */
    <K, V> CacheOperation<K, V> getCache(String name);
}
