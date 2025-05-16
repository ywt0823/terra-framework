package com.terra.framework.nova.tuner;

import lombok.Builder;
import lombok.Data;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

/**
 * 调优结果
 * 包含调优过程的最终结果
 *
 * @author terra-nova
 */
@Data
@Builder
public class TuningResult {
    
    /**
     * 结果ID
     */
    private String resultId;
    
    /**
     * 上下文ID
     */
    private String contextId;
    
    /**
     * 任务类型
     */
    private TuningContext.TaskType taskType;
    
    /**
     * 优化目标
     */
    private TuningContext.OptimizationGoal optimizationGoal;
    
    /**
     * 目标模型
     */
    private String targetModel;
    
    /**
     * 目标提供商
     */
    private String targetProvider;
    
    /**
     * 最优参数集
     */
    @Builder.Default
    private Map<String, Object> optimalParameters = new HashMap<>();
    
    /**
     * 调优器名称
     */
    private String tunerName;
    
    /**
     * 迭代次数
     */
    private int iterations;
    
    /**
     * 创建时间
     */
    @Builder.Default
    private Instant createdAt = Instant.now();
    
    /**
     * 相关指标
     */
    private TuningMetrics metrics;
    
    /**
     * 是否收敛
     */
    private boolean converged;
    
    /**
     * 相对改进率
     * 相对于初始参数的改进百分比
     */
    private double relativeImprovement;
    
    /**
     * 从上下文和指标创建结果
     *
     * @param context 调优上下文
     * @param parameters 最优参数
     * @param metrics 指标数据
     * @param tunerName 调优器名称
     * @param converged 是否收敛
     * @param relativeImprovement 相对改进率
     * @return 调优结果
     */
    public static TuningResult fromContext(TuningContext context, Map<String, Object> parameters,
                                          TuningMetrics metrics, String tunerName,
                                          boolean converged, double relativeImprovement) {
        return TuningResult.builder()
                .resultId(context.getContextId() + "-result")
                .contextId(context.getContextId())
                .taskType(context.getTaskType())
                .optimizationGoal(context.getOptimizationGoal())
                .targetModel(context.getTargetModel())
                .targetProvider(context.getTargetProvider())
                .optimalParameters(new HashMap<>(parameters))
                .tunerName(tunerName)
                .iterations(context.getIteration())
                .metrics(metrics)
                .converged(converged)
                .relativeImprovement(relativeImprovement)
                .build();
    }
} 