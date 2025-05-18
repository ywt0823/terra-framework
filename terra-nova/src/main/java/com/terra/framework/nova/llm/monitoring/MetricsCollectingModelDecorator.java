package com.terra.framework.nova.llm.monitoring;

import com.terra.framework.nova.llm.model.AIModel;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.ModelInfo;
import com.terra.framework.nova.llm.model.ModelResponse;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

/**
 * 指标收集模型装饰器，为模型添加指标收集功能
 *
 * @author terra-nova
 */
public class MetricsCollectingModelDecorator implements AIModel {

    /**
     * 被装饰的模型
     */
    private final AIModel delegate;

    /**
     * 指标收集器
     */
    private final MetricsCollector metricsCollector;

    /**
     * 构造函数
     *
     * @param delegate 被装饰的模型
     * @param metricsCollector 指标收集器
     */
    public MetricsCollectingModelDecorator(AIModel delegate, MetricsCollector metricsCollector) {
        this.delegate = delegate;
        this.metricsCollector = metricsCollector;
    }

    @Override
    public ModelResponse generate(String prompt, Map<String, Object> parameters) {
        String requestId = generateRequestId();
        TimingContext context = metricsCollector.recordInvocationStart(getModelInfo(), requestId, "GENERATE");

        try {
            ModelResponse response = delegate.generate(prompt, parameters);
            metricsCollector.recordInvocationEnd(context, response, true);

            if (response != null && response.getTokenUsage() != null) {
                metricsCollector.recordTokenUsage(getModelInfo(), response.getTokenUsage());
            }

            return response;
        } catch (Exception e) {
            metricsCollector.recordInvocationError(context, e);
            throw e;
        }
    }

    @Override
    public CompletableFuture<ModelResponse> generateAsync(String prompt, Map<String, Object> parameters) {
        String requestId = generateRequestId();
        TimingContext context = metricsCollector.recordInvocationStart(getModelInfo(), requestId, "GENERATE_ASYNC");

        return delegate.generateAsync(prompt, parameters)
                .whenComplete((response, error) -> {
                    if (error != null) {
                        metricsCollector.recordInvocationError(context, error);
                    } else {
                        metricsCollector.recordInvocationEnd(context, response, true);
                        if (response != null && response.getTokenUsage() != null) {
                            metricsCollector.recordTokenUsage(getModelInfo(), response.getTokenUsage());
                        }
                    }
                });
    }

    @Override
    public Publisher<String> generateStream(String prompt, Map<String, Object> parameters) {
        String requestId = generateRequestId();
        metricsCollector.recordInvocationStart(getModelInfo(), requestId, "GENERATE_STREAM");
        // 流式处理的指标收集比较复杂，这里简化处理
        return delegate.generateStream(prompt, parameters);
    }

    @Override
    public ModelResponse chat(List<Message> messages, Map<String, Object> parameters) {
        String requestId = generateRequestId();
        TimingContext context = metricsCollector.recordInvocationStart(getModelInfo(), requestId, "CHAT");

        try {
            ModelResponse response = delegate.chat(messages, parameters);
            metricsCollector.recordInvocationEnd(context, response, true);

            if (response != null && response.getTokenUsage() != null) {
                metricsCollector.recordTokenUsage(getModelInfo(), response.getTokenUsage());
            }

            return response;
        } catch (Exception e) {
            metricsCollector.recordInvocationError(context, e);
            throw e;
        }
    }

    @Override
    public CompletableFuture<ModelResponse> chatAsync(List<Message> messages, Map<String, Object> parameters) {
        String requestId = generateRequestId();
        TimingContext context = metricsCollector.recordInvocationStart(getModelInfo(), requestId, "CHAT_ASYNC");

        return delegate.chatAsync(messages, parameters)
                .whenComplete((response, error) -> {
                    if (error != null) {
                        metricsCollector.recordInvocationError(context, error);
                    } else {
                        metricsCollector.recordInvocationEnd(context, response, true);
                        if (response != null && response.getTokenUsage() != null) {
                            metricsCollector.recordTokenUsage(getModelInfo(), response.getTokenUsage());
                        }
                    }
                });
    }

    @Override
    public Publisher<String> chatStream(List<Message> messages, Map<String, Object> parameters) {
        String requestId = generateRequestId();
        metricsCollector.recordInvocationStart(getModelInfo(), requestId, "CHAT_STREAM");
        // 流式处理的指标收集比较复杂，这里简化处理
        return delegate.chatStream(messages, parameters);
    }

    @Override
    public ModelInfo getModelInfo() {
        return delegate.getModelInfo();
    }

    @Override
    public void init() {
        delegate.init();
    }

    @Override
    public void close() {
        delegate.close();
    }

    /**
     * 生成请求ID
     *
     * @return 请求ID
     */
    private String generateRequestId() {
        return UUID.randomUUID().toString();
    }
}
