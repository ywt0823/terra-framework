package com.terra.framework.geyser.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

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
}
