package com.terra.framework.nova.prompt.service.impl;

import com.terra.framework.nova.prompt.Prompt;
import com.terra.framework.nova.prompt.service.PromptService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 带缓存的提示词服务实现
 *
 * @author terra-nova
 */
public class CachingPromptService implements PromptService {

    private static final Logger log = LoggerFactory.getLogger(CachingPromptService.class);

    private final PromptService delegate;
    private final long ttlSeconds;
    private final int maxSize;
    private final Map<String, CacheEntry> cache = new ConcurrentHashMap<>();

    private static class CacheEntry {
        final String content;
        final Instant expireTime;

        CacheEntry(String content, long ttlSeconds) {
            this.content = content;
            this.expireTime = Instant.now().plusSeconds(ttlSeconds);
        }

        boolean isExpired() {
            return Instant.now().isAfter(expireTime);
        }
    }

    public CachingPromptService(PromptService delegate, long ttlSeconds, int maxSize) {
        this.delegate = delegate;
        this.ttlSeconds = ttlSeconds;
        this.maxSize = maxSize;
    }

    @Override
    public String render(String templateId, Map<String, Object> variables) {
        String cacheKey = generateCacheKey(templateId, variables);
        CacheEntry entry = cache.get(cacheKey);

        if (entry != null && !entry.isExpired()) {
            log.debug("Cache hit for template: {}", templateId);
            return entry.content;
        }

        String content = delegate.render(templateId, variables);
        if (cache.size() >= maxSize) {
            cleanupExpiredEntries();
            if (cache.size() >= maxSize) {
                log.warn("Cache is full, removing oldest entry");
                cache.remove(cache.keySet().iterator().next());
            }
        }

        cache.put(cacheKey, new CacheEntry(content, ttlSeconds));
        return content;
    }

    @Override
    public Prompt createPrompt(String templateId, Map<String, Object> variables) {
        return delegate.createPrompt(templateId, variables);
    }

    @Scheduled(fixedRate = 300000) // 每5分钟清理一次过期缓存
    public void cleanupExpiredEntries() {
        int beforeSize = cache.size();
        cache.entrySet().removeIf(entry -> entry.getValue().isExpired());
        int removedCount = beforeSize - cache.size();
        if (removedCount > 0) {
            log.debug("Cleaned up {} expired cache entries", removedCount);
        }
    }

    private String generateCacheKey(String templateId, Map<String, Object> variables) {
        return templateId + ":" + variables.hashCode();
    }

    public void clearCache() {
        cache.clear();
        log.info("Cache cleared");
    }

    public int getCacheSize() {
        return cache.size();
    }
}
