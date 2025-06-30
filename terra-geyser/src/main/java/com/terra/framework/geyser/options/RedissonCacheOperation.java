package com.terra.framework.geyser.options;

import org.redisson.api.RMapCache;
import org.redisson.api.RedissonClient;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;

/**
 * An adapter for Redisson's RMapCache to implement the CacheOperation interface.
 * This class translates generic cache operations into Redisson-specific commands
 * and simulates expire-after-access behavior.
 *
 * @author AI
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 */
public class RedissonCacheOperation<K, V> implements CacheOperation<K, V> {

    private final RMapCache<K, V> rMapCache;
    private final RedissonClient redissonClient;
    private final boolean expireAfterAccess;
    private final long expireDuration;
    private final TimeUnit timeUnit;

    public RedissonCacheOperation(RedissonClient redissonClient, RMapCache<K, V> rMapCache, boolean expireAfterAccess, long expireDuration, TimeUnit timeUnit) {
        this.rMapCache = rMapCache;
        this.redissonClient = redissonClient;
        this.expireAfterAccess = expireAfterAccess;
        this.expireDuration = expireDuration;
        this.timeUnit = timeUnit;
    }

    @Override
    public V get(K key, Callable<V> callable) {
        V value = rMapCache.get(key);
        if (value != null) {
            if (expireAfterAccess) {
                // Simulate expire-after-access by re-putting the value with the same TTL
                put(key, value);
            }
            return value;
        }

        try {
            V newValue = callable.call();
            if (newValue != null) {
                put(key, newValue);
            }
            return newValue;
        } catch (Exception e) {
            throw new RuntimeException("Failed to load value from callable for key: " + key, e);
        }
    }

    @Override
    public V getIfPresent(K key) {
        V value = rMapCache.get(key);
        if (value != null && expireAfterAccess) {
            // Simulate expire-after-access by re-putting the value with the same TTL
            put(key, value);
        }
        return value;
    }

    @Override
    public void put(K key, V value) {
        if (expireDuration > 0 && timeUnit != null) {
            rMapCache.put(key, value, expireDuration, timeUnit);
        } else {
            rMapCache.put(key, value);
        }
    }

    @Override
    public void putAll(Map<K, V> map) {
        if (expireDuration > 0 && timeUnit != null) {
            var batch = redissonClient.createBatch();
            map.forEach((k, v) -> batch.getMapCache(rMapCache.getName()).putAsync(k, v, expireDuration, timeUnit));
            batch.execute();
        } else {
            rMapCache.putAll(map);
        }
    }

    @Override
    public void invalidate(K key) {
        rMapCache.remove(key);
    }

    @Override
    @SuppressWarnings("unchecked")
    public void invalidateAll(List<K> keys) {
        if(keys == null || keys.isEmpty()){
            return;
        }
        // Redisson's fastRemove expects a varargs of keys, not an array.
        // We need to convert the list to an array of the correct type.
        rMapCache.fastRemove(keys.toArray((K[]) new Object[0]));
    }

    @Override
    public void invalidateAll() {
        rMapCache.clear();
    }

    @Override
    public String getStats() {
        return String.format("CacheName: %s, Size: %d, Type: Redisson", getName(), rMapCache.size());
    }

    @Override
    public String getName() {
        return rMapCache.getName();
    }
} 