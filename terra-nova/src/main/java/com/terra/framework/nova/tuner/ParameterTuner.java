package com.terra.framework.nova.tuner;

import java.util.Map;

/**
 * 参数调优器接口
 * 用于自动优化模型请求参数
 *
 * @author terra-nova
 */
public interface ParameterTuner {
    
    /**
     * 优化参数
     *
     * @param parameters 初始参数
     * @param context 调优上下文
     * @return 优化后的参数
     */
    Map<String, Object> tuneParameters(Map<String, Object> parameters, TuningContext context);
    
    /**
     * 根据结果更新调优器
     *
     * @param parameters 使用的参数
     * @param context 调优上下文
     * @param result 调用结果
     * @param metrics 结果指标
     */
    void updateWithResult(Map<String, Object> parameters, TuningContext context, 
                         String result, TuningMetrics metrics);
    
    /**
     * 获取调优器名称
     *
     * @return 调优器名称
     */
    String getName();
    
    /**
     * 重置调优器状态
     */
    void reset();
} 