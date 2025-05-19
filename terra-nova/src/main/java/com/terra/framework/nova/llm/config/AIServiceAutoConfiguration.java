package com.terra.framework.nova.llm.config;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.blend.DefaultModelBlender;
import com.terra.framework.nova.llm.blend.ModelBlender;
import com.terra.framework.nova.llm.cache.InMemoryResponseCache;
import com.terra.framework.nova.llm.cache.ResponseCache;
import com.terra.framework.nova.llm.model.*;
import com.terra.framework.nova.llm.monitoring.LoggingMetricsCollector;
import com.terra.framework.nova.llm.monitoring.MetricsCollector;
import com.terra.framework.nova.llm.properties.*;
import com.terra.framework.nova.llm.retry.DefaultRetryExecutor;
import com.terra.framework.nova.llm.retry.RetryExecutor;
import com.terra.framework.nova.llm.service.BlenderService;
import com.terra.framework.nova.llm.service.EnhancedAIService;
import com.terra.framework.nova.llm.service.EnhancedDefaultAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;

/**
 * AI服务自动配置
 *
 * @author terra-nova
 */
@Slf4j
@EnableConfigurationProperties({
    AIServiceProperties.class,
    RetryProperties.class,
    CacheProperties.class,
    RoutingProperties.class,
    MonitoringProperties.class,
    BlenderProperties.class
})
@ConditionalOnProperty(prefix = "terra.framework.ai", name = "enabled", havingValue = "true", matchIfMissing = true)
public class AIServiceAutoConfiguration {


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
    @Bean
    @ConditionalOnMissingBean
    public AIModelManager aiModelManager(
        HttpClientUtils httpClientUtils,
        RetryExecutor retryExecutor,
        ResponseCache responseCache,
        MetricsCollector metricsCollector,
        ModelDecoratorOptions decoratorOptions,
        AIServiceProperties aiServiceProperties) {

        AIModelManager modelManager = new AIModelManager(httpClientUtils);

        // 配置装饰器
        modelManager.setDecoratorOptions(decoratorOptions);
        modelManager.setRetryExecutor(retryExecutor);
        modelManager.setResponseCache(responseCache);
        modelManager.setMetricsCollector(metricsCollector);

        // 注册模型配置
        aiServiceProperties.getModels().forEach((modelId, modelProperties) -> {
            try {
                // 构建基本配置
                ModelConfig.ModelConfigBuilder builder = ModelConfig.builder()
                    .modelId(modelId)
                    .endpoint(modelProperties.getEndpoint())
                    .timeout(modelProperties.getTimeout())
                    .streamSupport(modelProperties.isStreamSupport())
                    .defaultParameters(new HashMap<>(modelProperties.getDefaultParameters()));

                // 设置模型类型
                if (modelProperties.getType() != null) {
                    builder.modelType(ModelType.valueOf(modelProperties.getType().toUpperCase()));
                }

                // 构建认证配置
                AuthConfig.AuthConfigBuilder authBuilder = AuthConfig.builder();

                if (modelProperties.getAuthType() != null) {
                    authBuilder.authType(AuthType.valueOf(modelProperties.getAuthType().toUpperCase()));
                } else if (modelProperties.getApiKey() != null) {
                    authBuilder.authType(AuthType.API_KEY);
                } else if (modelProperties.getApiKeyId() != null && modelProperties.getApiKeySecret() != null) {
                    authBuilder.authType(AuthType.AK_SK);
                } else if (modelProperties.getAuthToken() != null) {
                    authBuilder.authType(AuthType.BEARER_TOKEN);
                }

                authBuilder
                    .apiKey(modelProperties.getApiKey())
                    .apiKeyId(modelProperties.getApiKeyId())
                    .apiKeySecret(modelProperties.getApiKeySecret())
                    .authToken(modelProperties.getAuthToken())
                    .organizationId(modelProperties.getOrganizationId())
                    .projectId(modelProperties.getProjectId());

                builder.authConfig(authBuilder.build());

                // 如果有重试配置，设置重试配置
                if (modelProperties.getRetry() != null) {
                    RetryConfig retryConfig = RetryConfig.builder()
                        .maxRetries(modelProperties.getRetry().getMaxRetries())
                        .initialDelayMs(modelProperties.getRetry().getInitialDelayMs())
                        .maxDelayMs(modelProperties.getRetry().getMaxDelayMs())
                        .backoffMultiplier(modelProperties.getRetry().getBackoffMultiplier())
                        .build();

                    if (modelProperties.getRetry().getRetryableErrors() != null) {
                        for (String error : modelProperties.getRetry().getRetryableErrors()) {
                            retryConfig.addRetryableErrors(error);
                        }
                    }

                    builder.retryConfig(retryConfig);
                }

                // 注册模型配置
                modelManager.registerConfig(modelId, builder.build());
//                modelManager.refreshModel(modelId);
            } catch (Exception e) {
                // 记录异常但不中断其他模型的注册
                log.error("Failed to register model configuration for {}: {}", modelId, e.getMessage(), e);
            }
        });

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
