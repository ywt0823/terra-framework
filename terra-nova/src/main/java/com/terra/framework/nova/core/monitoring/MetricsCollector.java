package com.terra.framework.nova.core.monitoring;

import com.terra.framework.nova.core.model.ModelInfo;
import com.terra.framework.nova.core.model.ModelResponse;
import com.terra.framework.nova.core.model.TokenUsage;

/**
 * 指标收集器接口
 *
 * @author terra-nova
 */
public interface MetricsCollector {

    /**
     * 记录模型调用开始
     *
     * @param modelInfo 模型信息
     * @param requestId 请求ID
     * @param operationType 操作类型（如：generate, chat）
     * @return 计时上下文，用于计算耗时
     */
    TimingContext recordInvocationStart(ModelInfo modelInfo, String requestId, String operationType);

    /**
     * 记录模型调用结束
     *
     * @param context 计时上下文
     * @param response 模型响应
     * @param successful 是否成功
     */
    void recordInvocationEnd(TimingContext context, ModelResponse response, boolean successful);

    /**
     * 记录模型调用异常
     *
     * @param context 计时上下文
     * @param exception 异常
     */
    void recordInvocationError(TimingContext context, Throwable exception);

    /**
     * 记录令牌使用情况
     *
     * @param modelInfo 模型信息
     * @param tokenUsage 令牌使用情况
     */
    void recordTokenUsage(ModelInfo modelInfo, TokenUsage tokenUsage);

    /**
     * 记录模型延迟
     *
     * @param modelInfo 模型信息
     * @param latencyMs 延迟时间（毫秒）
     */
    void recordLatency(ModelInfo modelInfo, long latencyMs);

    /**
     * 记录重试次数
     *
     * @param modelInfo 模型信息
     * @param retryCount 重试次数
     */
    void recordRetryCount(ModelInfo modelInfo, int retryCount);

    /**
     * 记录模型错误
     *
     * @param modelInfo 模型信息
     * @param errorType 错误类型
     */
    void recordError(ModelInfo modelInfo, String errorType);

    /**
     * 获取指标收集器名称
     *
     * @return 名称
     */
    String getName();
}
