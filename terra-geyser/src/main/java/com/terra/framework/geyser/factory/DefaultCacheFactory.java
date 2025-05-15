package com.terra.framework.geyser.factory;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.terra.framework.geyser.options.CacheOperation;
import com.terra.framework.geyser.options.DefaultCacheOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Guava缓存工厂实现
 *
 * @author terra
 */
@Slf4j
public final class DefaultCacheFactory implements CacheFactory {

    private final Map<String, CacheOperation<?, ?>> caches = new ConcurrentHashMap<>();

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> CacheOperation<K, V> createAccessCache(String name, int maxSize, long expireAfterAccess, TimeUnit timeUnit) {
        return (CacheOperation<K, V>) caches.computeIfAbsent(name, n -> {
            log.info("创建Guava缓存[{}]，最大容量：{}，访问后过期时间：{} {}", name, maxSize, expireAfterAccess, timeUnit);
            Cache<K, V> cache = CacheBuilder.newBuilder()
                    .maximumSize(maxSize)
                    .expireAfterAccess(expireAfterAccess, timeUnit)
                    .recordStats()
                    .build();
            return new DefaultCacheOperation<>(name, cache);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> CacheOperation<K, V> createWriteCache(String name, int maxSize, long expireAfterWrite, TimeUnit timeUnit) {
        return (CacheOperation<K, V>) caches.computeIfAbsent(name, n -> {
            log.info("创建Guava缓存[{}]，最大容量：{}，写入后过期时间：{} {}", name, maxSize, expireAfterWrite, timeUnit);
            Cache<K, V> cache = CacheBuilder.newBuilder()
                    .maximumSize(maxSize)
                    .expireAfterWrite(expireAfterWrite, timeUnit)
                    .recordStats()
                    .build();
            return new DefaultCacheOperation<>(name, cache);
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <K, V> CacheOperation<K, V> getCache(String name) {
        return (CacheOperation<K, V>) caches.get(name);
    }
} 