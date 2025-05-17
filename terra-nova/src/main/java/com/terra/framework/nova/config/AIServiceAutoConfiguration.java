package com.terra.framework.nova.config;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.blend.DefaultModelBlender;
import com.terra.framework.nova.blend.MergeStrategy;
import com.terra.framework.nova.blend.ModelBlender;
import com.terra.framework.nova.cache.InMemoryResponseCache;
import com.terra.framework.nova.cache.ResponseCache;
import com.terra.framework.nova.properties.AIServiceProperties;
import com.terra.framework.nova.model.AIModelManager;
import com.terra.framework.nova.model.ModelDecoratorOptions;
import com.terra.framework.nova.model.RetryConfig;
import com.terra.framework.nova.monitoring.LoggingMetricsCollector;
import com.terra.framework.nova.monitoring.MetricsCollector;
import com.terra.framework.nova.retry.DefaultRetryExecutor;
import com.terra.framework.nova.retry.RetryExecutor;
import com.terra.framework.nova.service.BlenderService;
import com.terra.framework.nova.service.EnhancedAIService;
import com.terra.framework.nova.service.EnhancedDefaultAIService;
import lombok.Data;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * AI服务自动配置
 *
 * @author terra-nova
 */
@EnableConfigurationProperties({
    AIServiceProperties.class,
    AIServiceAutoConfiguration.RetryProperties.class,
    AIServiceAutoConfiguration.CacheProperties.class,
    AIServiceAutoConfiguration.RoutingProperties.class,
    AIServiceAutoConfiguration.MonitoringProperties.class,
    AIServiceAutoConfiguration.BlenderProperties.class
})
@ConditionalOnProperty(prefix = "terra.framework.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AIServiceAutoConfiguration {

    /**
     * 重试相关配置
     */
    @Data
    @ConfigurationProperties(prefix = "terra.nova.retry")
    public static class RetryProperties {
        /**
         * 是否启用重试功能
         */
        private boolean enabled = true;

        /**
         * 最大重试次数
         */
        private int maxRetries = 3;

        /**
         * 初始重试延迟（毫秒）
         */
        private long initialDelayMs = 1000;

        /**
         * 最大重试延迟（毫秒）
         */
        private long maxDelayMs = 10000;

        /**
         * 退避乘数
         */
        private double backoffMultiplier = 2.0;

        /**
         * 转换为RetryConfig
         *
         * @return RetryConfig
         */
        public RetryConfig toRetryConfig() {
            return RetryConfig.builder()
                .maxRetries(maxRetries)
                .initialDelayMs(initialDelayMs)
                .maxDelayMs(maxDelayMs)
                .backoffMultiplier(backoffMultiplier)
                .build();
        }
    }

    /**
     * 缓存相关配置
     */
    @Data
    @ConfigurationProperties(prefix = "terra.nova.cache")
    public static class CacheProperties {
        /**
         * 是否启用缓存功能
         */
        private boolean enabled = true;

        /**
         * 默认缓存过期时间（秒）
         */
        private int defaultTtlSeconds = 3600;
    }

    /**
     * 路由相关配置
     */
    @Data
    @ConfigurationProperties(prefix = "terra.nova.routing")
    public static class RoutingProperties {
        /**
         * 是否启用模型路由功能
         */
        private boolean enabled = true;


        /**
         * 模型健康检查失败阈值
         */
        private int healthCheckFailureThreshold = 3;

        /**
         * 模型健康恢复阈值
         */
        private int healthCheckRecoveryThreshold = 2;
    }

    /**
     * 监控相关配置
     */
    @Data
    @ConfigurationProperties(prefix = "terra.nova.monitoring")
    public static class MonitoringProperties {
        /**
         * 是否启用监控功能
         */
        private boolean enabled = true;
    }

    /**
     * 混合器相关配置
     */
    @Data
    @ConfigurationProperties(prefix = "terra.nova.blend")
    public static class BlenderProperties {
        /**
         * 是否启用模型混合功能
         */
        private boolean enabled = true;

        /**
         * 默认合并策略
         */
        private MergeStrategy mergeStrategy = MergeStrategy.WEIGHTED;

        /**
         * 是否在启动时自动添加所有模型
         */
        private boolean autoAddModels = true;

        /**
         * 线程池大小（0表示使用处理器数量决定）
         */
        private int threadPoolSize = 0;
    }

    /**
     * 配置装饰器选项
     *
     * @param retryProperties      重试属性
     * @param cacheProperties      缓存属性
     * @param routingProperties    路由属性
     * @param monitoringProperties 监控属性
     * @return 装饰器选项
     */
    @Bean
    @ConditionalOnMissingBean
    public ModelDecoratorOptions modelDecoratorOptions(
        RetryProperties retryProperties,
        CacheProperties cacheProperties,
        RoutingProperties routingProperties,
        MonitoringProperties monitoringProperties) {

        return ModelDecoratorOptions.builder()
            .retryEnabled(retryProperties.isEnabled())
            .cacheEnabled(cacheProperties.isEnabled())
            .metricsEnabled(monitoringProperties.isEnabled())
            .routingEnabled(routingProperties.isEnabled())
            .retryConfig(retryProperties.toRetryConfig())
            .defaultCacheTtlSeconds(cacheProperties.getDefaultTtlSeconds())
            .build();
    }

    /**
     * 配置模型管理器
     *
     * @param httpClientUtils  HTTP客户端工具
     * @param retryExecutor    重试执行器
     * @param responseCache    响应缓存
     * @param metricsCollector 指标收集器
     * @param decoratorOptions 装饰器选项
     * @return 模型管理器
     */
    @Bean("aiModelManager")
    @ConditionalOnMissingBean
    public AIModelManager aiModelManager(
        HttpClientUtils httpClientUtils,
        RetryExecutor retryExecutor,
        ResponseCache responseCache,
        MetricsCollector metricsCollector,
        ModelDecoratorOptions decoratorOptions) {

        AIModelManager modelManager = new AIModelManager(httpClientUtils);

        // 配置装饰器
        modelManager.setDecoratorOptions(decoratorOptions);
        modelManager.setRetryExecutor(retryExecutor);
        modelManager.setResponseCache(responseCache);
        modelManager.setMetricsCollector(metricsCollector);

        return modelManager;
    }


    /**
     * 配置重试执行器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "terra.nova.retry", name = "enabled", havingValue = "true", matchIfMissing = true)
    public RetryExecutor retryExecutor() {
        return new DefaultRetryExecutor();
    }

    /**
     * 配置指标收集器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "terra.nova.monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
    public MetricsCollector metricsCollector() {
        return new LoggingMetricsCollector();
    }

    /**
     * 配置响应缓存
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "terra.nova.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ResponseCache responseCache(CacheProperties cacheProperties) {
        InMemoryResponseCache cache = new InMemoryResponseCache();
        InMemoryResponseCache.DEFAULT_TTL_SECONDS = cacheProperties.getDefaultTtlSeconds();
        return cache;
    }

    /**
     * 配置模型混合器
     *
     * @param properties 混合器配置属性
     * @return 模型混合器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "terra.nova.blend", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ModelBlender modelBlender(BlenderProperties properties) {
        DefaultModelBlender blender = new DefaultModelBlender(properties.getMergeStrategy());
        return blender;
    }

    /**
     * 配置混合器服务
     *
     * @param blender      模型混合器
     * @param modelManager 模型管理器
     * @param properties   混合器配置属性
     * @return 混合器服务
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "terra.nova.blend", name = "enabled", havingValue = "true", matchIfMissing = true)
    public BlenderService blenderService(
        ModelBlender blender,
        AIModelManager modelManager,
        BlenderProperties properties) {
        BlenderService service = new BlenderService(blender, modelManager);
        if (properties.isAutoAddModels()) {
            service.addAllAvailableModels();
        }
        return service;
    }

    /**
     * 配置增强型AI服务
     *
     * @param modelManager   模型管理器
     * @param properties     配置属性
     * @param blenderService 混合器服务
     * @return 增强型AI服务
     */
    @Bean
    @ConditionalOnMissingBean(EnhancedAIService.class)
    @ConditionalOnProperty(prefix = "terra.nova.blend", name = "enabled", havingValue = "true", matchIfMissing = true)
    public EnhancedAIService enhancedAIService(
        AIModelManager modelManager,
        AIServiceProperties properties,
        BlenderService blenderService) {

        return new EnhancedDefaultAIService(
            modelManager,
            properties.getDefaultModelId(),
            blenderService
        );
    }
}
