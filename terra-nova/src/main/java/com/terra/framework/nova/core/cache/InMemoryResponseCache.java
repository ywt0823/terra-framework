package com.terra.framework.nova.core.cache;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terra.framework.nova.core.model.ModelResponse;

/**
 * 基于内存的响应缓存实现
 *
 * @author terra-nova
 */
public class InMemoryResponseCache implements ResponseCache {

    private static final Logger log = LoggerFactory.getLogger(InMemoryResponseCache.class);

    /**
     * 默认缓存过期时间（秒）
     */
    public static  int DEFAULT_TTL_SECONDS = 3600; // 1小时

    /**
     * 缓存清理间隔（秒）
     */
    private static final int CLEANUP_INTERVAL_SECONDS = 300; // 5分钟

    /**
     * 缓存条目
     */
    private static class CacheEntry {
        final ModelResponse response;
        final Instant expireTime;

        CacheEntry(ModelResponse response, int ttlSeconds) {
            this.response = response;
            this.expireTime = Instant.now().plusSeconds(ttlSeconds);
        }

        boolean isExpired() {
            return Instant.now().isAfter(expireTime);
        }
    }

    /**
     * 缓存数据
     */
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    /**
     * 缓存清理线程
     */
    private final ScheduledExecutorService cleanupExecutor;

    /**
     * 构造函数
     */
    public InMemoryResponseCache() {
        cleanupExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread thread = new Thread(r, "cache-cleanup-thread");
            thread.setDaemon(true);
            return thread;
        });

        // 定期清理过期缓存
        cleanupExecutor.scheduleAtFixedRate(
                this::cleanupExpiredEntries,
                CLEANUP_INTERVAL_SECONDS,
                CLEANUP_INTERVAL_SECONDS,
                TimeUnit.SECONDS
        );

        log.info("内存响应缓存已初始化，默认过期时间: {}秒，清理间隔: {}秒", DEFAULT_TTL_SECONDS, CLEANUP_INTERVAL_SECONDS);
    }

    @Override
    public ModelResponse get(String cacheKey) {
        CacheEntry entry = cache.get(cacheKey);

        if (entry == null) {
            return null;
        }

        if (entry.isExpired()) {
            cache.remove(cacheKey);
            return null;
        }

        log.debug("缓存命中: {}", cacheKey);
        return entry.response;
    }

    @Override
    public void put(String cacheKey, ModelResponse response, int ttlSeconds) {
        cache.put(cacheKey, new CacheEntry(response, ttlSeconds));
        log.debug("添加到缓存: {}, TTL: {}秒", cacheKey, ttlSeconds);
    }

    @Override
    public void put(String cacheKey, ModelResponse response) {
        put(cacheKey, response, DEFAULT_TTL_SECONDS);
    }

    @Override
    public boolean contains(String cacheKey) {
        CacheEntry entry = cache.get(cacheKey);
        if (entry == null) {
            return false;
        }

        if (entry.isExpired()) {
            cache.remove(cacheKey);
            return false;
        }

        return true;
    }

    @Override
    public void remove(String cacheKey) {
        cache.remove(cacheKey);
        log.debug("从缓存中移除: {}", cacheKey);
    }

    @Override
    public void clear() {
        cache.clear();
        log.info("缓存已清空");
    }

    @Override
    public int size() {
        cleanupExpiredEntries();
        return cache.size();
    }

    /**
     * 清理过期的缓存条目
     */
    private void cleanupExpiredEntries() {
        int initialSize = cache.size();

        cache.entrySet().removeIf(entry -> {
            boolean expired = entry.getValue().isExpired();
            if (expired) {
                log.debug("移除过期缓存: {}", entry.getKey());
            }
            return expired;
        });

        int removedCount = initialSize - cache.size();
        if (removedCount > 0) {
            log.info("已清理 {} 个过期缓存条目，当前缓存大小: {}", removedCount, cache.size());
        }
    }

    /**
     * 关闭缓存
     */
    public void shutdown() {
        cleanupExecutor.shutdown();
        try {
            if (!cleanupExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                cleanupExecutor.shutdownNow();
            }
        } catch (InterruptedException e) {
            cleanupExecutor.shutdownNow();
            Thread.currentThread().interrupt();
        }
        log.info("内存响应缓存已关闭");
    }
}
