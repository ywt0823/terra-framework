package com.terra.framework.autoconfigure.geyser.monitor;

import com.terra.framework.geyser.options.CacheOperation;
import com.terra.framework.geyser.util.CacheUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 缓存监控服务
 * 提供缓存使用情况监控和统计
 *
 * @author terra
 */
@Slf4j
public class CacheMonitorService {

    private final CacheUtils cacheUtils;
    private final boolean isMonitorEnabled;
    private final long monitorLogInterval;

    // 用于从统计信息中提取命中率的正则表达式
    private static final Pattern HIT_RATE_PATTERN = Pattern.compile("命中率: (\\d+\\.\\d+)%");

    public CacheMonitorService(CacheUtils cacheUtils, boolean isMonitorEnabled, long monitorLogInterval) {
        this.cacheUtils = cacheUtils;
        this.isMonitorEnabled = isMonitorEnabled;
        this.monitorLogInterval = monitorLogInterval;
    }

    /**
     * 定时收集并输出缓存统计信息
     */
    @Scheduled(fixedRateString = "${terra.cache.monitor-log-interval:300000}")
    public void logCacheStats() {
        if (!isMonitorEnabled) {
            return;
        }

        Map<String, String> statsMap = cacheUtils.getAllCacheStats();
        log.info("缓存使用情况统计 ========================================");

        for (Map.Entry<String, String> entry : statsMap.entrySet()) {
            String cacheName = entry.getKey();
            String stats = entry.getValue();
            log.info("缓存[{}] 统计: {}", cacheName, stats);

            // 检查命中率是否低于警告阈值
            Double hitRate = extractHitRate(stats);
            if (hitRate != null && hitRate < monitorLogInterval) {
                log.warn("缓存[{}]命中率较低({}%), 低于警告阈值({}%)",
                    cacheName, hitRate, monitorLogInterval);
            }
        }

        log.info("缓存统计结束 ========================================");
    }

    /**
     * 从统计信息字符串中提取命中率
     */
    private Double extractHitRate(String stats) {
        Matcher matcher = HIT_RATE_PATTERN.matcher(stats);
        if (matcher.find()) {
            try {
                return Double.parseDouble(matcher.group(1));
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * 获取指定缓存的健康状态
     */
    public CacheHealthInfo getCacheHealth(String cacheName) {
        CacheOperation<?, ?> cache = cacheUtils.getCache(cacheName);
        if (cache == null) {
            return null;
        }

        String stats = cache.getStats();
        Double hitRate = extractHitRate(stats);

        CacheHealthInfo healthInfo = new CacheHealthInfo();
        healthInfo.setCacheName(cacheName);
        healthInfo.setStats(stats);
        healthInfo.setHitRate(hitRate != null ? hitRate : 0.0);
        healthInfo.setHealthy(hitRate != null && hitRate >= monitorLogInterval);

        return healthInfo;
    }

    /**
     * 缓存健康信息
     */
    public static class CacheHealthInfo {
        private String cacheName;
        private String stats;
        private double hitRate;
        private boolean healthy;

        // Getters and setters
        public String getCacheName() {
            return cacheName;
        }

        public void setCacheName(String cacheName) {
            this.cacheName = cacheName;
        }

        public String getStats() {
            return stats;
        }

        public void setStats(String stats) {
            this.stats = stats;
        }

        public double getHitRate() {
            return hitRate;
        }

        public void setHitRate(double hitRate) {
            this.hitRate = hitRate;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public void setHealthy(boolean healthy) {
            this.healthy = healthy;
        }
    }
}
