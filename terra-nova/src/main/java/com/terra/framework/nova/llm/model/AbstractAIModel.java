package com.terra.framework.nova.llm.model;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.exception.ModelException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

import lombok.extern.slf4j.Slf4j;

/**
 * 抽象模型实现
 *
 * @author terra-nova
 */
@Slf4j
public abstract class AbstractAIModel implements AIModel {

    /**
     * 模型配置
     */
    protected final ModelConfig config;

    /**
     * HTTP客户端工具
     */
    protected final HttpClientUtils httpClientUtils;

    /**
     * 模型状态
     */
    protected ModelStatus status = ModelStatus.INITIALIZING;

    /**
     * 构造函数
     *
     * @param config 模型配置
     * @param httpClientUtils HTTP客户端工具
     */
    protected AbstractAIModel(ModelConfig config, HttpClientUtils httpClientUtils) {
        this.config = config;
        this.httpClientUtils = httpClientUtils;
    }

    @Override
    public CompletableFuture<ModelResponse> generateAsync(String prompt, Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> generate(prompt, parameters), Executors.newSingleThreadExecutor());
    }

    @Override
    public CompletableFuture<ModelResponse> chatAsync(List<Message> messages, Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> chat(messages, parameters), Executors.newSingleThreadExecutor());
    }

    /**
     * 构建请求参数，合并默认参数和传入参数
     *
     * @param parameters 传入参数
     * @return 合并后的参数
     */
    protected Map<String, Object> buildParameters(Map<String, Object> parameters) {
        Map<String, Object> mergedParams = (parameters != null) ? parameters : Map.of();

        // 如果存在默认参数，合并
        if (config.getDefaultParameters() != null && !config.getDefaultParameters().isEmpty()) {
            Map<String, Object> result = Map.copyOf(config.getDefaultParameters());

            // 传入的参数覆盖默认参数
            for (Map.Entry<String, Object> entry : mergedParams.entrySet()) {
                result.put(entry.getKey(), entry.getValue());
            }

            return result;
        }

        return mergedParams;
    }

    /**
     * 获取参数中的布尔值
     *
     * @param parameters 参数
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 布尔值
     */
    protected boolean getBooleanParam(Map<String, Object> parameters, String key, boolean defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return defaultValue;
    }

    /**
     * 获取参数中的整数值
     *
     * @param parameters 参数
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 整数值
     */
    protected int getIntParam(Map<String, Object> parameters, String key, int defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).intValue();
        }
        return defaultValue;
    }

    /**
     * 获取参数中的浮点值
     *
     * @param parameters 参数
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 浮点值
     */
    protected double getDoubleParam(Map<String, Object> parameters, String key, double defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    /**
     * 获取参数中的字符串值
     *
     * @param parameters 参数
     * @param key 参数名
     * @param defaultValue 默认值
     * @return 字符串值
     */
    protected String getStringParam(Map<String, Object> parameters, String key, String defaultValue) {
        Object value = parameters.get(key);
        if (value instanceof String) {
            return (String) value;
        }
        return defaultValue;
    }

    /**
     * 创建异常
     *
     * @param message 异常消息
     * @return 模型异常
     */
    protected ModelException createException(String message) {
        log.error(message);
        status = ModelStatus.ERROR;
        return new ModelException(message);
    }

    /**
     * 创建异常
     *
     * @param message 异常消息
     * @param cause 原因异常
     * @return 模型异常
     */
    protected ModelException createException(String message, Throwable cause) {
        log.error(message, cause);
        status = ModelStatus.ERROR;
        return new ModelException(message, cause);
    }
}
