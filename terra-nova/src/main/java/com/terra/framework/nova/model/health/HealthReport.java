package com.terra.framework.nova.model.health;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;

/**
 * 健康检查报告
 *
 * @author terra-nova
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HealthReport {
    
    /**
     * 报告ID
     */
    private String reportId;
    
    /**
     * 客户端ID
     */
    private String clientId;
    
    /**
     * 检查时间
     */
    private ZonedDateTime checkTime;
    
    /**
     * 是否可用
     */
    private boolean available;
    
    /**
     * 响应延迟（毫秒）
     */
    private double latency;
    
    /**
     * 是否发生错误
     */
    private boolean hasError;
    
    /**
     * 错误消息
     */
    private String errorMessage;
    
    /**
     * 响应状态码
     */
    private int statusCode;
} 