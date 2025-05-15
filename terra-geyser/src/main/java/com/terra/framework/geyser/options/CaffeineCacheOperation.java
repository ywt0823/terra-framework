package com.terra.framework.geyser.options;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.stats.CacheStats;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * Caffeine缓存操作实现
 *
 * @author terra
 */
@Slf4j
public final class CaffeineCacheOperation<K, V> implements CacheOperation<K, V> {

    private final Cache<K, V> cache;
    private final String name;

    public CaffeineCacheOperation(String name, Cache<K, V> cache) {
        this.name = name;
        this.cache = cache;
    }

    @Override
    public V get(K key, Callable<V> callable) {
        try {
            return cache.get(key, k -> {
                try {
                    return callable.call();
                } catch (Exception e) {
                    log.error("从缓存[{}]获取键[{}]值失败", name, key, e);
                    return null;
                }
            });
        } catch (Exception e) {
            log.error("从缓存[{}]获取键[{}]值出现未知错误", name, key, e);
            return null;
        }
    }

    @Override
    public V getIfPresent(K key) {
        try {
            return cache.getIfPresent(key);
        } catch (Exception e) {
            log.error("从缓存[{}]获取键[{}]值失败", name, key, e);
            return null;
        }
    }

    @Override
    public void put(K key, V value) {
        try {
            cache.put(key, value);
        } catch (Exception e) {
            log.error("向缓存[{}]放入键[{}]值失败", name, key, e);
        }
    }

    @Override
    public void putAll(Map<K, V> map) {
        try {
            cache.putAll(map);
        } catch (Exception e) {
            log.error("向缓存[{}]批量放入值失败", name, e);
        }
    }

    @Override
    public void invalidate(K key) {
        try {
            cache.invalidate(key);
        } catch (Exception e) {
            log.error("从缓存[{}]移除键[{}]失败", name, key, e);
        }
    }

    @Override
    public void invalidateAll(List<K> keys) {
        try {
            cache.invalidateAll(keys);
        } catch (Exception e) {
            log.error("从缓存[{}]批量移除键失败", name, e);
        }
    }

    @Override
    public void invalidateAll() {
        try {
            cache.invalidateAll();
        } catch (Exception e) {
            log.error("清空缓存[{}]失败", name, e);
        }
    }

    @Override
    public String getStats() {
        CacheStats stats = cache.stats();
        return String.format("命中率: %.2f%%, 平均加载时间: %.2fms, " +
                        "驱逐次数: %d, 总请求次数: %d, 总命中次数: %d, 总加载次数: %d",
                stats.hitRate() * 100,
                stats.averageLoadPenalty() / 1_000_000.0,
                stats.evictionCount(),
                stats.requestCount(),
                stats.hitCount(),
                stats.loadCount());
    }

    @Override
    public String getName() {
        return name;
    }

    /**
     * 获取原始Caffeine Cache实例
     */
    public Cache<K, V> getNativeCache() {
        return cache;
    }
} 