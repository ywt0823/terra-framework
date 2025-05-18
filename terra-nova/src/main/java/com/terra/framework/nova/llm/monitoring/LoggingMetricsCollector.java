package com.terra.framework.nova.llm.monitoring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.ModelInfo;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.llm.model.TokenUsage;

/**
 * 基于日志的指标收集器
 *
 * @author terra-nova
 */
public class LoggingMetricsCollector implements MetricsCollector {

    private static final Logger log = LoggerFactory.getLogger(LoggingMetricsCollector.class);

    @Override
    public TimingContext recordInvocationStart(ModelInfo modelInfo, String requestId, String operationType) {
        log.info("模型调用开始：模型={}, 请求ID={}, 操作={}",
                 modelInfo.getModelId(), requestId, operationType);
        return TimingContext.create(modelInfo, requestId, operationType);
    }

    @Override
    public void recordInvocationEnd(TimingContext context, ModelResponse response, boolean successful) {
        long elapsedMs = context.getElapsedTimeMs();
        ModelInfo modelInfo = context.getModelInfo();

        if (successful) {
            log.info("模型调用成功：模型={}, 请求ID={}, 操作={}, 耗时={}ms",
                     modelInfo.getModelId(), context.getRequestId(), context.getOperationType(), elapsedMs);

            if (response != null && response.getTokenUsage() != null) {
                TokenUsage usage = response.getTokenUsage();
                log.info("令牌使用情况：模型={}, 请求ID={}, 输入令牌={}, 输出令牌={}, 总令牌={}",
                         modelInfo.getModelId(), context.getRequestId(),
                         usage.getPromptTokens(), usage.getCompletionTokens(), usage.getTotalTokens());
            }
        } else {
            log.warn("模型调用失败：模型={}, 请求ID={}, 操作={}, 耗时={}ms",
                      modelInfo.getModelId(), context.getRequestId(), context.getOperationType(), elapsedMs);
        }
    }

    @Override
    public void recordInvocationError(TimingContext context, Throwable exception) {
        long elapsedMs = context.getElapsedTimeMs();
        ModelInfo modelInfo = context.getModelInfo();

        String errorType = "UNKNOWN";
        if (exception instanceof ModelException) {
            errorType = ((ModelException) exception).getErrorType().toString();
        }

        log.error("模型调用异常：模型={}, 请求ID={}, 操作={}, 耗时={}ms, 错误类型={}, 错误信息={}",
                  modelInfo.getModelId(), context.getRequestId(), context.getOperationType(),
                  elapsedMs, errorType, exception.getMessage());
    }

    @Override
    public void recordTokenUsage(ModelInfo modelInfo, TokenUsage tokenUsage) {
        if (tokenUsage != null) {
            log.info("令牌使用记录：模型={}, 输入令牌={}, 输出令牌={}, 总令牌={}",
                     modelInfo.getModelId(), tokenUsage.getPromptTokens(),
                     tokenUsage.getCompletionTokens(), tokenUsage.getTotalTokens());
        }
    }

    @Override
    public void recordLatency(ModelInfo modelInfo, long latencyMs) {
        log.info("模型延迟：模型={}, 延迟={}ms", modelInfo.getModelId(), latencyMs);
    }

    @Override
    public void recordRetryCount(ModelInfo modelInfo, int retryCount) {
        if (retryCount > 0) {
            log.info("模型调用重试：模型={}, 重试次数={}", modelInfo.getModelId(), retryCount);
        }
    }

    @Override
    public void recordError(ModelInfo modelInfo, String errorType) {
        log.error("模型错误：模型={}, 错误类型={}", modelInfo.getModelId(), errorType);
    }

    @Override
    public String getName() {
        return "LoggingMetricsCollector";
    }
}
