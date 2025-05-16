package com.terra.framework.nova.model.router;

/**
 * 路由策略枚举
 *
 * @author terra-nova
 */
public enum RoutingStrategy {
    
    /**
     * 总是使用默认模型
     */
    DEFAULT_ONLY,
    
    /**
     * 优先使用用户指定的模型，失败时回退到默认模型
     */
    USER_PREFERRED,
    
    /**
     * 根据成本优化选择模型
     */
    COST_OPTIMIZED,
    
    /**
     * 根据性能优化选择模型
     */
    PERFORMANCE_OPTIMIZED,
    
    /**
     * 平衡成本和性能
     */
    COST_PERFORMANCE_BALANCED,
    
    /**
     * 根据可用性选择模型（首选在线且响应快的模型）
     */
    AVAILABILITY_OPTIMIZED,
    
    /**
     * 轮询所有可用模型
     */
    ROUND_ROBIN
} 