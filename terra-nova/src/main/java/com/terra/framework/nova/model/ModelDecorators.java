package com.terra.framework.nova.model;

import com.terra.framework.nova.cache.CachingModelDecorator;
import com.terra.framework.nova.cache.ResponseCache;
import com.terra.framework.nova.monitoring.MetricsCollectingModelDecorator;
import com.terra.framework.nova.monitoring.MetricsCollector;
import com.terra.framework.nova.retry.RetryExecutor;
import com.terra.framework.nova.retry.RetryingModelDecorator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 模型装饰器工具类
 *
 * @author terra-nova
 */
public class ModelDecorators {

    private static final Logger log = LoggerFactory.getLogger(ModelDecorators.class);

    private ModelDecorators() {
        // 工具类不应被实例化
    }

    /**
     * 应用所有装饰器
     *
     * @param model            原始模型
     * @param options          装饰器选项
     * @param retryExecutor    重试执行器
     * @param responseCache    响应缓存
     * @param metricsCollector 指标收集器
     * @return 装饰后的模型
     */
    public static AIModel applyAll(
        AIModel model,
        ModelDecoratorOptions options,
        RetryExecutor retryExecutor,
        ResponseCache responseCache,
        MetricsCollector metricsCollector) {

        // 按照特定顺序应用装饰器（从内到外）
        AIModel decorated = model;

        // 1. 指标收集装饰器（最内层）
        if (options.isMetricsEnabled() && metricsCollector != null) {
            log.debug("为模型 {} 应用指标收集装饰器", model.getModelInfo().getModelId());
            decorated = new MetricsCollectingModelDecorator(decorated, metricsCollector);
        }

        // 2. 重试装饰器
        if (options.isRetryEnabled() && retryExecutor != null) {
            log.debug("为模型 {} 应用重试装饰器", model.getModelInfo().getModelId());
            decorated = new RetryingModelDecorator(decorated, retryExecutor, options.getRetryConfig());
        }

        // 3. 缓存装饰器
        if (options.isCacheEnabled() && responseCache != null) {
            log.debug("为模型 {} 应用缓存装饰器", model.getModelInfo().getModelId());
            CachingModelDecorator cacheDecorator = new CachingModelDecorator(decorated, responseCache);
            decorated = cacheDecorator;
        }


        return decorated;
    }

    /**
     * 应用默认装饰器
     *
     * @param model            原始模型
     * @param retryExecutor    重试执行器
     * @param responseCache    响应缓存
     * @param metricsCollector 指标收集器
     * @return 装饰后的模型
     */
    public static AIModel applyDefaults(
        AIModel model,
        RetryExecutor retryExecutor,
        ResponseCache responseCache,
        MetricsCollector metricsCollector) {
        return applyAll(
            model,
            ModelDecoratorOptions.getDefault(),
            retryExecutor,
            responseCache,
            metricsCollector
        );
    }
}
