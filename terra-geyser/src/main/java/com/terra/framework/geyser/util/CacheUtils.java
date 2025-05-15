package com.terra.framework.geyser.util;

import com.terra.framework.geyser.factory.CacheFactory;
import com.terra.framework.geyser.options.CacheOperation;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存工具 <br/>
 *
 * @author terra
 */
@Slf4j
public class CacheUtils {

    private final CacheFactory cacheFactory;
    private final Map<String, CacheOperation<?, ?>> cacheRegistry = new HashMap<>();

    public CacheUtils(CacheFactory cacheFactory) {
        this.cacheFactory = cacheFactory;
    }

    /**
     * 创建或获取一个访问后过期的缓存
     */
    public <K, V> CacheOperation<K, V> createAccessCache(String name, int maxSize, long expireAfterAccess, TimeUnit timeUnit) {
        return cacheFactory.createAccessCache(name, maxSize, expireAfterAccess, timeUnit);
    }

    /**
     * 创建或获取一个写入后过期的缓存
     */
    public <K, V> CacheOperation<K, V> createWriteCache(String name, int maxSize, long expireAfterWrite, TimeUnit timeUnit) {
        return cacheFactory.createWriteCache(name, maxSize, expireAfterWrite, timeUnit);
    }

    /**
     * 获取指定名称的缓存
     */
    public <K, V> CacheOperation<K, V> getCache(String name) {
        return cacheFactory.getCache(name);
    }
    
    /**
     * 获取所有缓存的统计信息
     */
    public Map<String, String> getAllCacheStats() {
        Map<String, String> statsMap = new HashMap<>();
        
        // 遍历缓存工厂中注册的所有缓存，收集统计信息
        for (Map.Entry<String, CacheOperation<?, ?>> entry : cacheRegistry.entrySet()) {
            statsMap.put(entry.getKey(), entry.getValue().getStats());
        }
        
        return statsMap;
    }
    
    /**
     * 清除指定缓存的所有数据
     */
    public void clearCache(String cacheName) {
        CacheOperation<?, ?> cache = getCache(cacheName);
        if (cache != null) {
            cache.invalidateAll();
            log.info("已清空缓存: {}", cacheName);
        }
    }
    
    /**
     * 清除所有缓存
     */
    public void clearAllCaches() {
        for (Map.Entry<String, CacheOperation<?, ?>> entry : cacheRegistry.entrySet()) {
            entry.getValue().invalidateAll();
        }
        log.info("已清空所有缓存");
    }
    
    /**
     * 注册缓存到注册表
     */
    @SuppressWarnings("unchecked")
    public <K, V> void registerCache(String name, CacheOperation<K, V> cache) {
        cacheRegistry.put(name, cache);
    }
}
