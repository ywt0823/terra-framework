package com.terra.framework.nova.core.retry;

import com.terra.framework.nova.core.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

/**
 * 重试模型装饰器，为模型添加重试功能
 *
 * @author terra-nova
 */
public class RetryingModelDecorator implements AIModel {

    private static final Logger log = LoggerFactory.getLogger(RetryingModelDecorator.class);

    /**
     * 被装饰的模型
     */
    private final AIModel delegate;

    /**
     * 重试执行器
     */
    private final RetryExecutor retryExecutor;

    /**
     * 重试配置
     */
    private RetryConfig retryConfig;

    /**
     * 构造函数
     *
     * @param delegate      被装饰的模型
     * @param retryExecutor 重试执行器
     */
    public RetryingModelDecorator(AIModel delegate, RetryExecutor retryExecutor) {
        this(delegate, retryExecutor, RetryConfig.builder().build());
    }

    /**
     * 构造函数
     *
     * @param delegate      被装饰的模型
     * @param retryExecutor 重试执行器
     * @param retryConfig   重试配置
     */
    public RetryingModelDecorator(AIModel delegate, RetryExecutor retryExecutor, RetryConfig retryConfig) {
        this.delegate = delegate;
        this.retryExecutor = retryExecutor;
        this.retryConfig = retryConfig;
    }

    /**
     * 设置重试配置
     *
     * @param retryConfig 重试配置
     */
    public void setRetryConfig(RetryConfig retryConfig) {
        this.retryConfig = retryConfig;
    }

    @Override
    public ModelResponse generate(String prompt, Map<String, Object> parameters) {
        try {
            return retryExecutor.execute(
                () -> delegate.generate(prompt, parameters),
                getRetryConfig(parameters)
            );
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            // 对于已检查异常，包装成运行时异常
            throw new RuntimeException("重试后执行失败", e);
        }
    }

    @Override
    public CompletableFuture<ModelResponse> generateAsync(String prompt, Map<String, Object> parameters) {
        CompletableFuture<ModelResponse> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                future.complete(generate(prompt, parameters));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public Publisher<String> generateStream(String prompt, Map<String, Object> parameters) {
        // 流式处理不支持重试
        return delegate.generateStream(prompt, parameters);
    }

    @Override
    public ModelResponse chat(List<Message> messages, Map<String, Object> parameters) {
        try {
            return retryExecutor.execute(
                () -> delegate.chat(messages, parameters),
                getRetryConfig(parameters)
            );
        } catch (Exception e) {
            if (e instanceof RuntimeException) {
                throw (RuntimeException) e;
            }
            throw new RuntimeException("重试后执行失败", e);
        }
    }

    @Override
    public CompletableFuture<ModelResponse> chatAsync(List<Message> messages, Map<String, Object> parameters) {
        CompletableFuture<ModelResponse> future = new CompletableFuture<>();

        CompletableFuture.runAsync(() -> {
            try {
                future.complete(chat(messages, parameters));
            } catch (Exception e) {
                future.completeExceptionally(e);
            }
        });

        return future;
    }

    @Override
    public Publisher<String> chatStream(List<Message> messages, Map<String, Object> parameters) {
        // 流式处理不支持重试
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
     * 获取重试配置
     *
     * @param parameters 参数
     * @return 重试配置
     */
    private RetryConfig getRetryConfig(Map<String, Object> parameters) {
        // 可以从参数中获取自定义的重试配置
        if (parameters != null && parameters.containsKey("retry_config")) {
            Object retryConfigObj = parameters.get("retry_config");
            if (retryConfigObj instanceof RetryConfig) {
                return (RetryConfig) retryConfigObj;
            }
        }
        return this.retryConfig;
    }
}
