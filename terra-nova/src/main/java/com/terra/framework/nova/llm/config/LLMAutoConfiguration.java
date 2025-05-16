package com.terra.framework.nova.llm.config;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.core.ModelConfig;
import com.terra.framework.nova.llm.core.ModelType;
import com.terra.framework.nova.llm.manager.ModelManager;
import com.terra.framework.nova.llm.model.base.LLMModel;
import com.terra.framework.nova.llm.properties.LLMProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM自动配置类
 */
@EnableConfigurationProperties(LLMProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.llm", name = "enabled", havingValue = "true", matchIfMissing = true)
public class LLMAutoConfiguration {

    /**
     * 配置ModelManager
     */
    @Bean
    @ConditionalOnMissingBean
    public ModelManager modelManager(LLMProperties properties, HttpClientUtils httpClientUtils) {
        ModelManager manager = new ModelManager(httpClientUtils);

        // 配置DeepSeek模型
        if (properties.getDeepseek().getApiKey() != null) {
            ModelConfig deepseekConfig = ModelConfig.builder()
                .type(ModelType.DEEPSEEK)
                .apiKey(properties.getDeepseek().getApiKey())
                .apiEndpoint(properties.getDeepseek().getApiEndpoint())
                .modelName(properties.getDeepseek().getModelName())
                .temperature(properties.getDeepseek().getTemperature())
                .maxTokens(properties.getDeepseek().getMaxTokens())
                .timeoutMs(properties.getDeepseek().getTimeoutMs())
                .maxRetries(properties.getDeepseek().getMaxRetries())
                .extraParams(properties.getDeepseek().getExtraParams())
                .build();
            manager.registerConfig("deepseek", deepseekConfig);
        }

        // 配置通义千问模型
        if (properties.getTongyi().getApiKey() != null) {
            ModelConfig tongyiConfig = ModelConfig.builder()
                .type(ModelType.TONGYI)
                .apiKey(properties.getTongyi().getApiKey())
                .apiEndpoint(properties.getTongyi().getApiEndpoint())
                .modelName(properties.getTongyi().getModelName())
                .temperature(properties.getTongyi().getTemperature())
                .maxTokens(properties.getTongyi().getMaxTokens())
                .timeoutMs(properties.getTongyi().getTimeoutMs())
                .maxRetries(properties.getTongyi().getMaxRetries())
                .extraParams(properties.getTongyi().getExtraParams())
                .build();
            manager.registerConfig("tongyi", tongyiConfig);
        }

        // 配置Dify模型
        if (properties.getDify().getApiKey() != null && properties.getDify().getAppId() != null) {
            Map<String, Object> extraParams = new HashMap<>(properties.getDify().getExtraParams());
            extraParams.put("appId", properties.getDify().getAppId());
            extraParams.put("useKnowledgeBase", properties.getDify().isUseKnowledgeBase());
            extraParams.put("knowledgeBaseId", properties.getDify().getKnowledgeBaseId());

            ModelConfig difyConfig = ModelConfig.builder()
                .type(ModelType.DIFY)
                .apiKey(properties.getDify().getApiKey())
                .apiEndpoint(properties.getDify().getApiEndpoint())
                .temperature(properties.getDify().getTemperature())
                .maxTokens(properties.getDify().getMaxTokens())
                .timeoutMs(properties.getDify().getTimeoutMs())
                .maxRetries(properties.getDify().getMaxRetries())
                .extraParams(extraParams)
                .build();
            manager.registerConfig("dify", difyConfig);
        }

        // 配置百度文心模型
        if (properties.getBaidu().getApiKey() != null && properties.getBaidu().getSecretKey() != null) {
            Map<String, Object> extraParams = new HashMap<>(properties.getBaidu().getExtraParams());
            extraParams.put("secretKey", properties.getBaidu().getSecretKey());
            
            ModelConfig baiduConfig = ModelConfig.builder()
                .type(ModelType.BAIDU_WENXIN)
                .apiKey(properties.getBaidu().getApiKey())
                .apiEndpoint(properties.getBaidu().getApiEndpoint())
                .modelName(properties.getBaidu().getModelName())
                .temperature(properties.getBaidu().getTemperature())
                .maxTokens(properties.getBaidu().getMaxTokens())
                .timeoutMs(properties.getBaidu().getTimeoutMs())
                .maxRetries(properties.getBaidu().getMaxRetries())
                .extraParams(extraParams)
                .build();
            manager.registerConfig("baidu", baiduConfig);
        }

        return manager;
    }

    /**
     * 配置默认的DeepSeek模型
     */
    @Bean
    @ConditionalOnMissingBean(name = "deepseekModel")
    @ConditionalOnProperty(prefix = "terra.nova.llm.deepseek", name = "api-key")
    public LLMModel deepseekModel(ModelManager modelManager) {
        return modelManager.getModel("deepseek");
    }

    /**
     * 配置默认的通义千问模型
     */
    @Bean
    @ConditionalOnMissingBean(name = "tongyiModel")
    @ConditionalOnProperty(prefix = "terra.nova.llm.tongyi", name = "api-key")
    public LLMModel tongyiModel(ModelManager modelManager) {
        return modelManager.getModel("tongyi");
    }

    /**
     * 配置默认的Dify模型
     */
    @Bean
    @ConditionalOnMissingBean(name = "difyModel")
    @ConditionalOnProperty(prefix = "terra.nova.llm.dify", name = "api-key")
    public LLMModel difyModel(ModelManager modelManager) {
        return modelManager.getModel("dify");
    }

    /**
     * 配置默认的百度文心模型
     */
    @Bean
    @ConditionalOnMissingBean(name = "baiduModel")
    @ConditionalOnProperty(prefix = "terra.nova.llm.baidu", name = "api-key")
    public LLMModel baiduModel(ModelManager modelManager) {
        return modelManager.getModel("baidu");
    }
}
