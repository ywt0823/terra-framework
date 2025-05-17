package com.terra.framework.nova.model;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.cache.ResponseCache;
import com.terra.framework.nova.monitoring.MetricsCollector;
import com.terra.framework.nova.retry.RetryExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * AI模型管理器
 *
 * @author terra-nova
 */
@Slf4j
public class AIModelManager {

    /**
     * 模型缓存
     */
    private final Map<String, AIModel> modelCache = new ConcurrentHashMap<>();

    /**
     * 配置缓存
     */
    private final Map<String, ModelConfig> configCache = new ConcurrentHashMap<>();

    /**
     * HTTP客户端工具
     */
    private final HttpClientUtils httpClient;

    /**
     * 重试执行器
     */
    private RetryExecutor retryExecutor;

    /**
     * 响应缓存
     */
    private ResponseCache responseCache;

    /**
     * 指标收集器
     */
    private MetricsCollector metricsCollector;


    /**
     * 装饰器选项
     */
    private ModelDecoratorOptions decoratorOptions = ModelDecoratorOptions.getDefault();

    /**
     * 构造函数
     *
     * @param httpClient HTTP客户端工具
     */
    public AIModelManager(HttpClientUtils httpClient) {
        this.httpClient = httpClient;
    }

    /**
     * 设置装饰器选项
     *
     * @param decoratorOptions 装饰器选项
     */
    public void setDecoratorOptions(ModelDecoratorOptions decoratorOptions) {
        this.decoratorOptions = decoratorOptions;
    }

    /**
     * 设置重试执行器
     *
     * @param retryExecutor 重试执行器
     */
    public void setRetryExecutor(RetryExecutor retryExecutor) {
        this.retryExecutor = retryExecutor;
    }

    /**
     * 设置响应缓存
     *
     * @param responseCache 响应缓存
     */
    public void setResponseCache(ResponseCache responseCache) {
        this.responseCache = responseCache;
    }

    /**
     * 设置指标收集器
     *
     * @param metricsCollector 指标收集器
     */
    public void setMetricsCollector(MetricsCollector metricsCollector) {
        this.metricsCollector = metricsCollector;
    }


    /**
     * 设置是否启用重试
     *
     * @param retryEnabled 是否启用重试
     */
    public void setRetryEnabled(boolean retryEnabled) {
        this.decoratorOptions.setRetryEnabled(retryEnabled);
    }

    /**
     * 设置是否启用缓存
     *
     * @param cacheEnabled 是否启用缓存
     */
    public void setCacheEnabled(boolean cacheEnabled) {
        this.decoratorOptions.setCacheEnabled(cacheEnabled);
    }

    /**
     * 设置是否启用指标收集
     *
     * @param metricsEnabled 是否启用指标收集
     */
    public void setMetricsEnabled(boolean metricsEnabled) {
        this.decoratorOptions.setMetricsEnabled(metricsEnabled);
    }

    /**
     * 设置是否启用路由
     *
     * @param routingEnabled 是否启用路由
     */
    public void setRoutingEnabled(boolean routingEnabled) {
        this.decoratorOptions.setRoutingEnabled(routingEnabled);
    }

    /**
     * 获取模型实例
     *
     * @param modelId 模型ID
     * @return AI模型实例
     */
    public AIModel getModel(String modelId) {
        return modelCache.computeIfAbsent(modelId, this::createModel);
    }


    /**
     * 注册模型配置
     *
     * @param modelId 模型ID
     * @param config  模型配置
     */
    public void registerConfig(String modelId, ModelConfig config) {
        configCache.put(modelId, config);
    }

    /**
     * 刷新模型实例
     *
     * @param modelId 模型ID
     */
    public void refreshModel(String modelId) {
        AIModel model = modelCache.remove(modelId);
        if (model != null) {
            model.close();
        }
        getModel(modelId);
    }

    /**
     * 创建模型实例
     *
     * @param modelId 模型ID
     * @return AI模型实例
     */
    private AIModel createModel(String modelId) {
        ModelConfig config = loadConfig(modelId);
        AIModel model = AIModelFactory.createModel(config, httpClient);
        model.init();

        // 应用装饰器
        model = ModelDecorators.applyAll(
            model,
            decoratorOptions,
            retryExecutor,
            responseCache,
            metricsCollector
        );

        return model;
    }

    /**
     * 加载模型配置
     *
     * @param modelId 模型ID
     * @return 模型配置
     */
    private ModelConfig loadConfig(String modelId) {
        ModelConfig config = configCache.get(modelId);
        if (config == null) {
            throw new IllegalStateException("未找到模型配置: " + modelId);
        }
        return config;
    }

    /**
     * 关闭所有模型
     */
    public void shutdown() {
        log.info("关闭所有模型");
        modelCache.values().forEach(AIModel::close);
        modelCache.clear();
    }

    /**
     * 获取所有模型
     *
     * @return 所有模型列表
     */
    public List<AIModel> getAllModels() {
        return List.copyOf(modelCache.values());
    }

}
