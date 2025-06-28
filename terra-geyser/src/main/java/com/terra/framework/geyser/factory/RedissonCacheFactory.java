package com.terra.framework.geyser.factory;

import com.terra.framework.geyser.options.CacheOperation;
import com.terra.framework.geyser.options.RedissonCacheOperation;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

/**
 * @author Zeus
 * @date 2025年06月28日 18:02
 * @description RedissonCacheFactory for creating Redisson-based cache operations.
 */
@Slf4j
public final class RedissonCacheFactory implements CacheFactory {

    private final RedissonClient redissonClient;
    private final ConcurrentMap<String, CacheOperation<?, ?>> cacheMap = new ConcurrentHashMap<>();

    public RedissonCacheFactory(RedissonClient redissonClient) {
        this.redissonClient = redissonClient;
    }

    @Override
    public <K, V> CacheOperation<K, V> createAccessCache(String name, int maxSize, long expireAfterAccess, TimeUnit timeUnit) {
        return createCache(name, maxSize, expireAfterAccess, 0, timeUnit, true);
    }

    @Override
    public <K, V> CacheOperation<K, V> createWriteCache(String name, int maxSize, long expireAfterWrite, TimeUnit timeUnit) {
        return createCache(name, maxSize, 0, expireAfterWrite, timeUnit, false);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <K, V> CacheOperation<K, V> getCache(String name) {
        CacheOperation<K, V> cache = (CacheOperation<K, V>) cacheMap.get(name);
        if (cache == null) {
            RMapCache<K, V> rMapCache = redissonClient.getMapCache(name);
            if (rMapCache.isExists()) {
                cache = new RedissonCacheOperation<>(redissonClient, rMapCache, false, 0, null); // We don't know the original policy
                cacheMap.put(name, cache);
            }
        }
        return cache;
    }

    @SuppressWarnings("unchecked")
    private <K, V> CacheOperation<K, V> createCache(String name, int maxSize, long expireAfterAccess, long expireAfterWrite, TimeUnit timeUnit, boolean isAccessExpire) {
        return (CacheOperation<K, V>) cacheMap.computeIfAbsent(name, k -> {
            log.info("Creating Redisson cache: [{}], maxSize: [{}], expireAfterAccess: [{}], expireAfterWrite: [{}], timeUnit: [{}]",
                    name, maxSize, expireAfterAccess, expireAfterWrite, timeUnit);
            RMapCache<K, V> rMapCache = redissonClient.getMapCache(name);
            // Redisson's RMapCache size is managed on the Redis server side, `maxSize` is more of a logical hint here.
            return new RedissonCacheOperation<>(redissonClient, rMapCache, isAccessExpire, isAccessExpire ? expireAfterAccess : expireAfterWrite, timeUnit);
        });
    }
}
