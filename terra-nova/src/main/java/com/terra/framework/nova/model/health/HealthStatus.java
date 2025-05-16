package com.terra.framework.nova.model.health;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * 模型健康状态类
 *
 * @author terra-nova
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthStatus {
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 是否可用
     */
    private boolean available;
    
    /**
     * 平均响应延迟（毫秒）
     */
    private double latency;
    
    /**
     * 错误计数
     */
    private int errorCount;
    
    /**
     * 成功计数
     */
    private int successCount;
    
    /**
     * 最近一次检查时间
     */
    private ZonedDateTime lastCheckedTime;
    
    /**
     * 状态消息
     */
    private String message;
    
    /**
     * 可靠性指数 (0-1.0)
     */
    public double getReliabilityScore() {
        if (successCount + errorCount == 0) {
            return 0.0;
        }
        return (double) successCount / (successCount + errorCount);
    }
} 