package com.terra.framework.strata.helper;

import com.terra.framework.strata.helper.AutoCacheManager.MultiLevelCache;
import com.terra.framework.strata.helper.SqlMetricsCollector.HotSqlInfo;
import com.terra.framework.strata.helper.SqlMetricsCollector.HotTableInfo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 缓存控制器
 * 提供缓存管理和控制功能的接口
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class CacheController {

    private final AutoCacheManager cacheManager;
    private final SqlMetricsCollector metricsCollector;

    /**
     * 手动创建多级缓存
     */
    public <K, V> MultiLevelCache<K, V> createCache(String name, int localMaxSize, long localExpireTime, long redisExpireTime, TimeUnit timeUnit) {
        return cacheManager.createMultiLevelCache(name, localMaxSize, localExpireTime, redisExpireTime, timeUnit);
    }

    /**
     * 获取缓存
     */
    public <K, V> MultiLevelCache<K, V> getCache(String name) {
        return cacheManager.getMultiLevelCache(name);
    }

    /**
     * 清除指定缓存
     */
    public void invalidateCache(String name) {
        MultiLevelCache<?, ?> cache = cacheManager.getMultiLevelCache(name);
        if (cache != null) {
            cache.invalidateAll();
            log.info("手动清除缓存[{}]", name);
        }
    }

    /**
     * 清除与表相关的所有缓存
     */
    public void invalidateTableCache(String tableName) {
        // 清除热点表缓存
        if (metricsCollector.isHotTable(tableName)) {
            HotTableInfo hotTableInfo = metricsCollector.getHotTableInfo(tableName);
            if (hotTableInfo != null) {
                hotTableInfo.getLocalCache().invalidateAll();
                hotTableInfo.getRedisCache().invalidateAll();
                log.info("手动清除热点表[{}]的缓存", tableName);
            }
        }
        
        // 清除表名对应的多级缓存
        MultiLevelCache<?, ?> tableCache = cacheManager.getMultiLevelCache(tableName);
        if (tableCache != null) {
            tableCache.invalidateAll();
            log.info("手动清除表[{}]的多级缓存", tableName);
        }
    }

    /**
     * 清除与SQL ID相关的所有缓存
     */
    public void invalidateSqlCache(String sqlId) {
        if (metricsCollector.isHotSql(sqlId)) {
            HotSqlInfo hotSqlInfo = metricsCollector.getHotSqlInfo(sqlId);
            if (hotSqlInfo != null) {
                hotSqlInfo.getLocalCache().invalidateAll();
                hotSqlInfo.getRedisCache().invalidateAll();
                log.info("手动清除热点SQL[{}]的缓存", sqlId);
            }
        }
    }

    /**
     * 获取缓存统计信息
     */
    public Map<String, String> getCacheStats() {
        Map<String, String> stats = new HashMap<>();
        
        // 收集自定义多级缓存统计
        // TODO: 实现多级缓存统计信息收集
        
        // 收集热点SQL缓存统计
        for (String sqlId : metricsCollector.getHotSqlIds()) {
            HotSqlInfo hotSqlInfo = metricsCollector.getHotSqlInfo(sqlId);
            if (hotSqlInfo != null) {
                stats.put("热点SQL-本地-" + sqlId, hotSqlInfo.getLocalCache().getStats());
                stats.put("热点SQL-Redis-" + sqlId, hotSqlInfo.getRedisCache().getStats());
            }
        }
        
        // 收集热点表缓存统计
        for (String tableName : metricsCollector.getHotTableNames()) {
            HotTableInfo hotTableInfo = metricsCollector.getHotTableInfo(tableName);
            if (hotTableInfo != null) {
                stats.put("热点表-本地-" + tableName, hotTableInfo.getLocalCache().getStats());
                stats.put("热点表-Redis-" + tableName, hotTableInfo.getRedisCache().getStats());
            }
        }
        
        return stats;
    }
} 