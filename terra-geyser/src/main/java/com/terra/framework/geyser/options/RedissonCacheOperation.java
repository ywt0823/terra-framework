package com.terra.framework.geyser.options;

import com.terra.framework.geyser.config.CacheProperties;
import com.terra.framework.geyser.util.CacheNull;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
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
 * @param <K> the type of keys maintained by this cache
 * @param <V> the type of mapped values
 * @author AI
 */
@Slf4j
public class RedissonCacheOperation<K, V> implements CacheOperation<K, V> {

    private static final String LOCK_KEY_PREFIX = "lock_for_cache_key:";
    private final RMapCache<K, V> rMapCache;
    private final RedissonClient redissonClient;
    private final boolean expireAfterAccess;
    private final long expireDuration;
    private final TimeUnit timeUnit;
    private final CacheProperties.BreakdownProtection breakdownProtection;
    private final CacheProperties.PenetrationProtection penetrationProtection;

    public RedissonCacheOperation(RedissonClient redissonClient, RMapCache<K, V> rMapCache, boolean expireAfterAccess, long expireDuration, TimeUnit timeUnit, CacheProperties cacheProperties) {
        this.rMapCache = rMapCache;
        this.redissonClient = redissonClient;
        this.expireAfterAccess = expireAfterAccess;
        this.expireDuration = expireDuration;
        this.timeUnit = timeUnit;
        this.breakdownProtection = cacheProperties.getBreakdownProtection();
        this.penetrationProtection = cacheProperties.getPenetrationProtection();
    }

    @Override
    public V get(K key, Callable<V> callable) {
        V value = rMapCache.get(key);
        if (value != null) {
            if (value instanceof CacheNull) {
                return null;
            }
            if (expireAfterAccess) {
                put(key, value);
            }
            return value;
        }

        if (breakdownProtection.isEnabled()) {
            return loadWithBreakdownProtection(key, callable);
        } else {
            return loadValue(key, callable);
        }
    }

    private V loadWithBreakdownProtection(K key, Callable<V> callable) {
        RLock lock = redissonClient.getLock(LOCK_KEY_PREFIX + getName() + ":" + key);
        try {
            boolean acquired = lock.tryLock(breakdownProtection.getLockWaitTime(), breakdownProtection.getLockLeaseTime(), breakdownProtection.getTimeUnit());
            if (!acquired) {
                log.warn("Failed to acquire lock for cache key: {}, another thread may be loading it.", key);
                // Short sleep and retry from cache
                Thread.sleep(50);
                V retryValue = rMapCache.get(key);
                if (retryValue instanceof CacheNull) {
                    return null;
                }
                return retryValue;
            }
            // Acquired the lock, now we are responsible for loading the value.
            // Double-check if another thread loaded it while we were waiting for the lock.
            V doubleCheckValue = rMapCache.get(key);
            if (doubleCheckValue != null) {
                if (doubleCheckValue instanceof CacheNull) {
                    return null;
                }
                return doubleCheckValue;
            }
            return loadValue(key, callable);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Thread interrupted while waiting for cache lock for key: " + key, e);
        } finally {
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }
    }

    private V loadValue(K key, Callable<V> callable) {
        try {
            V newValue = callable.call();
            if (newValue == null) {
                if (penetrationProtection.isEnabled()) {
                    log.debug("Caching null value for key: {} with TTL: {} {}", key, penetrationProtection.getNullValueTtl(), penetrationProtection.getTimeUnit());
                    rMapCache.put(key, (V) CacheNull.INSTANCE, penetrationProtection.getNullValueTtl(), penetrationProtection.getTimeUnit());
                }
            } else {
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
        if (value instanceof CacheNull) {
            return null;
        }
        if (value != null && expireAfterAccess) {
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
        if (keys == null || keys.isEmpty()) {
            return;
        }
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
