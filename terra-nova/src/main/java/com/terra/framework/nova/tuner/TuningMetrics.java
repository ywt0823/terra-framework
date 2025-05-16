package com.terra.framework.nova.tuner;

import lombok.Builder;
import lombok.Data;

import java.time.Duration;
import java.time.Instant;

/**
 * 调优指标
 * 记录和计算调优结果的各项指标
 *
 * @author terra-nova
 */
@Data
@Builder
public class TuningMetrics {
    
    /**
     * 开始时间
     */
    @Builder.Default
    private Instant startTime = Instant.now();
    
    /**
     * 结束时间
     */
    private Instant endTime;
    
    /**
     * 响应时间（毫秒）
     */
    private long responseTimeMs;
    
    /**
     * 令牌数量
     */
    private int tokenCount;
    
    /**
     * 结果质量评分 (0-1)
     */
    private double qualityScore;
    
    /**
     * 相关性评分 (0-1)
     */
    private double relevanceScore;
    
    /**
     * 成本（美分）
     */
    private double cost;
    
    /**
     * 记录响应时间
     * 
     * @param startTimeMs 开始时间（毫秒）
     * @param endTimeMs 结束时间（毫秒）
     */
    public void recordResponseTime(long startTimeMs, long endTimeMs) {
        this.responseTimeMs = endTimeMs - startTimeMs;
    }
    
    /**
     * 设置结束时间并计算响应时间
     */
    public void setEndTime() {
        this.endTime = Instant.now();
        this.responseTimeMs = Duration.between(startTime, endTime).toMillis();
    }
    
    /**
     * 计算每个令牌的平均响应时间（毫秒）
     * 
     * @return 每个令牌的平均响应时间
     */
    public double getAvgTimePerToken() {
        return tokenCount > 0 ? (double) responseTimeMs / tokenCount : 0;
    }
    
    /**
     * 计算每个令牌的成本（美分）
     * 
     * @return 每个令牌的成本
     */
    public double getCostPerToken() {
        return tokenCount > 0 ? cost / tokenCount : 0;
    }
    
    /**
     * 计算综合得分
     * 基于质量、响应时间和成本的加权平均
     * 
     * @param qualityWeight 质量权重
     * @param speedWeight 速度权重
     * @param costWeight 成本权重
     * @return 综合得分 (0-1)
     */
    public double getCompositeScore(double qualityWeight, double speedWeight, double costWeight) {
        // 归一化各项指标
        double normalizedTime = responseTimeMs > 0 ? Math.min(1.0, 5000.0 / responseTimeMs) : 0;
        double normalizedCost = cost > 0 ? Math.min(1.0, 0.05 / cost) : 0;
        
        // 计算加权平均
        double totalWeight = qualityWeight + speedWeight + costWeight;
        return (qualityScore * qualityWeight + normalizedTime * speedWeight + normalizedCost * costWeight) / totalWeight;
    }
    
    /**
     * 基于优化目标计算综合得分
     * 
     * @param goal 优化目标
     * @return 综合得分 (0-1)
     */
    public double getCompositeScore(TuningContext.OptimizationGoal goal) {
        switch (goal) {
            case QUALITY:
                return getCompositeScore(0.8, 0.1, 0.1);
            case SPEED:
                return getCompositeScore(0.2, 0.7, 0.1);
            case COST:
                return getCompositeScore(0.2, 0.1, 0.7);
            case BALANCED:
            default:
                return getCompositeScore(0.33, 0.33, 0.34);
        }
    }
} 