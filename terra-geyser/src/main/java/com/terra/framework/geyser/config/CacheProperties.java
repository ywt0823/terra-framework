package com.terra.framework.geyser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.concurrent.TimeUnit;

/**
 * 缓存属性配置
 *
 * @author terra
 */
@Data
@ConfigurationProperties(prefix = "terra.cache")
public class CacheProperties {
    /**
     * 是否启用监控
     */
    private boolean monitorEnabled = true;

    /**
     * 命中率警告阈值(百分比)
     */
    private double hitRateWarningThreshold = 80.0;

    /**
     * 监控日志输出间隔(毫秒)
     */
    private long monitorLogInterval = 300000; // 5分钟

    /**
     * 缓存击穿保护配置
     */
    private BreakdownProtection breakdownProtection = new BreakdownProtection();

    /**
     * 缓存穿透保护配置
     */
    private PenetrationProtection penetrationProtection = new PenetrationProtection();

    @Data
    public static class BreakdownProtection {
        /**
         * 是否启用缓存击穿保护
         */
        private boolean enabled = true;

        /**
         * 获取分布式锁的等待时间
         */
        private long lockWaitTime = 5;

        /**
         * 分布式锁的租期
         */
        private long lockLeaseTime = 10;

        /**
         * 时间单位
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;
    }

    @Data
    public static class PenetrationProtection {
        /**
         * 是否启用缓存穿透保护（空值缓存）
         */
        private boolean enabled = true;

        /**
         * 空值缓存的TTL
         */
        private long nullValueTtl = 60;

        /**
         * 时间单位
         */
        private TimeUnit timeUnit = TimeUnit.SECONDS;
    }
}
