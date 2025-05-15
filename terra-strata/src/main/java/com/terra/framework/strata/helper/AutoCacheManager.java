package com.terra.framework.strata.helper;

import com.terra.framework.geyser.factory.CacheFactory;
import com.terra.framework.geyser.options.CacheOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * 自动缓存管理器
 * 负责管理本地缓存和Redis缓存
 */
@Slf4j
@Component
public class AutoCacheManager {

    // 本地缓存工厂
    private final CacheFactory localCacheFactory;
    
    // Redis缓存工厂
    private final CacheFactory redisCacheFactory;
    
    // 缓存注册表
    private final Map<String, MultiLevelCache> cacheRegistry = new ConcurrentHashMap<>();

    public AutoCacheManager(
            @Qualifier("defaultCacheFactory") CacheFactory localCacheFactory,
            @Qualifier("redisCacheFactory") CacheFactory redisCacheFactory) {
        this.localCacheFactory = localCacheFactory;
        this.redisCacheFactory = redisCacheFactory;
    }

    /**
     * 创建本地缓存
     * 
     * @param name 缓存名称
     * @param maxSize 最大元素数量
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @return 缓存操作实例
     */
    public <K, V> CacheOperation<K, V> createLocalCache(String name, int maxSize, long expireTime, TimeUnit timeUnit) {
        return localCacheFactory.createWriteCache(name, maxSize, expireTime, timeUnit);
    }

    /**
     * 创建Redis缓存
     * 
     * @param name 缓存名称
     * @param maxSize 最大元素数量（Redis忽略此参数）
     * @param expireTime 过期时间
     * @param timeUnit 时间单位
     * @return 缓存操作实例
     */
    public <K, V> CacheOperation<K, V> createRedisCache(String name, int maxSize, long expireTime, TimeUnit timeUnit) {
        return redisCacheFactory.createWriteCache(name, maxSize, expireTime, timeUnit);
    }

    /**
     * 创建多级缓存
     * 使用本地缓存作为一级缓存，Redis作为二级缓存
     * 
     * @param name 缓存名称
     * @param localMaxSize 本地缓存最大元素数量
     * @param localExpireTime 本地缓存过期时间
     * @param redisExpireTime Redis缓存过期时间
     * @param timeUnit 时间单位
     * @return 多级缓存实例
     */
    @SuppressWarnings("unchecked")
    public <K, V> MultiLevelCache<K, V> createMultiLevelCache(
            String name, 
            int localMaxSize, 
            long localExpireTime, 
            long redisExpireTime, 
            TimeUnit timeUnit) {
        
        return (MultiLevelCache<K, V>) cacheRegistry.computeIfAbsent(name, n -> {
            log.info("创建多级缓存[{}]，本地缓存大小：{}，本地过期时间：{} {}，Redis过期时间：{} {}", 
                    name, localMaxSize, localExpireTime, timeUnit, redisExpireTime, timeUnit);

            CacheOperation<K, V> localCache = createLocalCache(name + "_local", localMaxSize, localExpireTime, timeUnit);
            CacheOperation<K, V> redisCache = createRedisCache(name + "_redis", 0, redisExpireTime, timeUnit);
            
            return new MultiLevelCache<>(name, localCache, redisCache);
        });
    }

    /**
     * 获取已创建的多级缓存
     * 
     * @param name 缓存名称
     * @return 多级缓存实例，如果不存在返回null
     */
    @SuppressWarnings("unchecked")
    public <K, V> MultiLevelCache<K, V> getMultiLevelCache(String name) {
        return (MultiLevelCache<K, V>) cacheRegistry.get(name);
    }

    /**
     * 多级缓存实现
     * 组合本地缓存和Redis缓存，提供统一的接口
     */
    public static class MultiLevelCache<K, V> {
        private final String name;
        private final CacheOperation<K, V> localCache;
        private final CacheOperation<K, V> redisCache;

        public MultiLevelCache(String name, CacheOperation<K, V> localCache, CacheOperation<K, V> redisCache) {
            this.name = name;
            this.localCache = localCache;
            this.redisCache = redisCache;
        }

        /**
         * 获取缓存值，按照本地缓存 -> Redis缓存 -> 数据源的顺序查找
         * 
         * @param key 缓存键
         * @param loader 数据加载器
         * @return 缓存值
         */
        public V get(K key, java.util.concurrent.Callable<V> loader) {
            // 1. 先查本地缓存
            V value = localCache.getIfPresent(key);
            if (value != null) {
                return value;
            }

            // 2. 本地缓存未命中，查Redis缓存
            value = redisCache.getIfPresent(key);
            if (value != null) {
                // 将Redis中的值放入本地缓存
                localCache.put(key, value);
                return value;
            }

            // 3. Redis也未命中，调用数据加载器获取数据
            try {
                value = loader.call();
                if (value != null) {
                    // 将数据同时放入本地缓存和Redis缓存
                    localCache.put(key, value);
                    redisCache.put(key, value);
                }
                return value;
            } catch (Exception e) {
                log.error("从多级缓存[{}]获取键[{}]值失败", name, key, e);
                return null;
            }
        }

        /**
         * 检查缓存是否包含指定键
         * 
         * @param key 缓存键
         * @return 如果本地缓存或Redis缓存包含该键，返回true
         */
        public boolean contains(K key) {
            V value = localCache.getIfPresent(key);
            if (value != null) {
                return true;
            }
            
            return redisCache.getIfPresent(key) != null;
        }

        /**
         * 手动设置缓存值到所有层
         * 
         * @param key 缓存键
         * @param value 缓存值
         */
        public void put(K key, V value) {
            if (value != null) {
                localCache.put(key, value);
                redisCache.put(key, value);
            }
        }

        /**
         * 删除指定键的缓存
         * 
         * @param key 缓存键
         */
        public void invalidate(K key) {
            localCache.invalidate(key);
            redisCache.invalidate(key);
        }

        /**
         * 清空所有缓存
         */
        public void invalidateAll() {
            localCache.invalidateAll();
            redisCache.invalidateAll();
        }

        /**
         * 获取缓存名称
         */
        public String getName() {
            return name;
        }

        /**
         * 获取本地缓存实例
         */
        public CacheOperation<K, V> getLocalCache() {
            return localCache;
        }

        /**
         * 获取Redis缓存实例
         */
        public CacheOperation<K, V> getRedisCache() {
            return redisCache;
        }
    }
} 