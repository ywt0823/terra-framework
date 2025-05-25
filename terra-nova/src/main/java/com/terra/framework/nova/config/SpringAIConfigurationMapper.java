package com.terra.framework.nova.config;

import com.terra.framework.nova.properties.TerraNovaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring AI 配置映射器
 *
 * <p>将 Terra Nova 配置映射到 Spring AI 原生配置，实现：
 * <ul>
 *   <li>terra.nova.models.providers.openai -> spring.ai.openai</li>
 *   <li>terra.nova.models.providers.anthropic -> spring.ai.anthropic</li>
 *   <li>terra.nova.models.providers.ollama -> spring.ai.ollama</li>
 *   <li>terra.nova.vector-stores -> spring.ai.vectorstore</li>
 * </ul>
 *
 * @author terra-nova
 * @since 0.0.1
 */
public class SpringAIConfigurationMapper {

    private static final Logger logger = LoggerFactory.getLogger(SpringAIConfigurationMapper.class);

    private static final String TERRA_NOVA_PROPERTY_SOURCE_NAME = "terraNovaMappedProperties";

    /**
     * 将 Terra Nova 配置映射到 Spring AI 配置
     *
     * @param environment 环境配置
     * @param properties Terra Nova 属性
     */
    public void mapTerraNovaToSpringAI(ConfigurableEnvironment environment, TerraNovaProperties properties) {
        if (!properties.getSpringAi().isEnabled()) {
            logger.debug("Spring AI integration is disabled, skipping configuration mapping");
            return;
        }

        logger.info("Mapping Terra Nova configuration to Spring AI properties");

        Map<String, Object> mappedProperties = new HashMap<>();

        // 映射模型提供商配置
        mapModelProviders(mappedProperties, properties);

        // 映射向量存储配置
        mapVectorStores(mappedProperties, properties);

        // 添加映射的属性到环境中
        if (!mappedProperties.isEmpty()) {
            addMappedPropertiesToEnvironment(environment, mappedProperties);
            logger.info("Successfully mapped {} Terra Nova properties to Spring AI configuration",
                       mappedProperties.size());
        }
    }

    /**
     * 映射模型提供商配置
     */
    private void mapModelProviders(Map<String, Object> mappedProperties, TerraNovaProperties properties) {
        Map<String, TerraNovaProperties.Models.Provider> providers = properties.getModels().getProviders();

        for (Map.Entry<String, TerraNovaProperties.Models.Provider> entry : providers.entrySet()) {
            String providerName = entry.getKey();
            TerraNovaProperties.Models.Provider provider = entry.getValue();

            switch (providerName.toLowerCase()) {
                case "openai":
                    mapOpenAIProvider(mappedProperties, provider);
                    break;
                case "anthropic":
                    mapAnthropicProvider(mappedProperties, provider);
                    break;
                case "ollama":
                    mapOllamaProvider(mappedProperties, provider);
                    break;
                default:
                    logger.debug("Skipping mapping for unsupported provider: {}", providerName);
            }
        }
    }

    /**
     * 映射 OpenAI 提供商配置
     */
    private void mapOpenAIProvider(Map<String, Object> mappedProperties, TerraNovaProperties.Models.Provider provider) {
        if (provider.getApiKey() != null) {
            mappedProperties.put("spring.ai.openai.api-key", provider.getApiKey());
        }

        if (provider.getBaseUrl() != null) {
            mappedProperties.put("spring.ai.openai.base-url", provider.getBaseUrl());
        }

        // 映射默认聊天模型配置
        if (provider.getModels() != null && !provider.getModels().isEmpty()) {
            TerraNovaProperties.Models.Model defaultChatModel = provider.getModels().stream()
                .filter(model -> model.getType() == TerraNovaProperties.Models.ModelType.CHAT)
                .findFirst()
                .orElse(provider.getModels().get(0));

            if (defaultChatModel != null) {
                mappedProperties.put("spring.ai.openai.chat.options.model", defaultChatModel.getName());
                if (defaultChatModel.getTemperature() != null) {
                    mappedProperties.put("spring.ai.openai.chat.options.temperature", defaultChatModel.getTemperature());
                }
                if (defaultChatModel.getMaxTokens() != null) {
                    mappedProperties.put("spring.ai.openai.chat.options.max-tokens", defaultChatModel.getMaxTokens());
                }
            }
        }

        logger.debug("Mapped OpenAI provider configuration");
    }

    /**
     * 映射 Anthropic 提供商配置
     */
    private void mapAnthropicProvider(Map<String, Object> mappedProperties, TerraNovaProperties.Models.Provider provider) {
        if (provider.getApiKey() != null) {
            mappedProperties.put("spring.ai.anthropic.api-key", provider.getApiKey());
        }

        // 映射默认聊天模型配置
        if (provider.getModels() != null && !provider.getModels().isEmpty()) {
            TerraNovaProperties.Models.Model defaultChatModel = provider.getModels().stream()
                .filter(model -> model.getType() == TerraNovaProperties.Models.ModelType.CHAT)
                .findFirst()
                .orElse(provider.getModels().get(0));

            if (defaultChatModel != null) {
                mappedProperties.put("spring.ai.anthropic.chat.options.model", defaultChatModel.getName());
                if (defaultChatModel.getTemperature() != null) {
                    mappedProperties.put("spring.ai.anthropic.chat.options.temperature", defaultChatModel.getTemperature());
                }
                if (defaultChatModel.getMaxTokens() != null) {
                    mappedProperties.put("spring.ai.anthropic.chat.options.max-tokens", defaultChatModel.getMaxTokens());
                }
            }
        }

        logger.debug("Mapped Anthropic provider configuration");
    }

    /**
     * 映射 Ollama 提供商配置
     */
    private void mapOllamaProvider(Map<String, Object> mappedProperties, TerraNovaProperties.Models.Provider provider) {
        if (provider.getBaseUrl() != null) {
            mappedProperties.put("spring.ai.ollama.base-url", provider.getBaseUrl());
        }

        // 映射默认聊天模型配置
        if (provider.getModels() != null && !provider.getModels().isEmpty()) {
            TerraNovaProperties.Models.Model defaultChatModel = provider.getModels().stream()
                .filter(model -> model.getType() == TerraNovaProperties.Models.ModelType.CHAT)
                .findFirst()
                .orElse(provider.getModels().get(0));

            if (defaultChatModel != null) {
                mappedProperties.put("spring.ai.ollama.chat.options.model", defaultChatModel.getName());
                if (defaultChatModel.getTemperature() != null) {
                    mappedProperties.put("spring.ai.ollama.chat.options.temperature", defaultChatModel.getTemperature());
                }
            }
        }

        logger.debug("Mapped Ollama provider configuration");
    }

    /**
     * 映射向量存储配置
     */
    private void mapVectorStores(Map<String, Object> mappedProperties, TerraNovaProperties properties) {
        TerraNovaProperties.VectorStores vectorStores = properties.getVectorStores();

        // 映射 Redis 向量存储
        if (vectorStores.getRedis() != null) {
            TerraNovaProperties.VectorStores.Redis redis = vectorStores.getRedis();
            String redisUri = String.format("redis://%s:%d", redis.getHost(), redis.getPort());
            mappedProperties.put("spring.ai.vectorstore.redis.uri", redisUri);
            mappedProperties.put("spring.ai.vectorstore.redis.index", redis.getIndexName());
            mappedProperties.put("spring.ai.vectorstore.redis.prefix", "doc:");
        }

        // 映射 PostgreSQL 向量存储
        if (vectorStores.getPostgresql() != null) {
            TerraNovaProperties.VectorStores.PostgreSQL postgresql = vectorStores.getPostgresql();
            mappedProperties.put("spring.ai.vectorstore.pgvector.url", postgresql.getUrl());
            mappedProperties.put("spring.ai.vectorstore.pgvector.table-name", postgresql.getTableName());
            mappedProperties.put("spring.ai.vectorstore.pgvector.dimension", postgresql.getDimensions());
        }

        logger.debug("Mapped vector store configurations");
    }

    /**
     * 将映射的属性添加到环境中
     */
    private void addMappedPropertiesToEnvironment(ConfigurableEnvironment environment, Map<String, Object> mappedProperties) {
        MutablePropertySources propertySources = environment.getPropertySources();

        // 移除之前的映射属性源（如果存在）
        if (propertySources.contains(TERRA_NOVA_PROPERTY_SOURCE_NAME)) {
            propertySources.remove(TERRA_NOVA_PROPERTY_SOURCE_NAME);
        }

        // 添加新的映射属性源，优先级较高
        MapPropertySource mappedPropertySource = new MapPropertySource(TERRA_NOVA_PROPERTY_SOURCE_NAME, mappedProperties);
        propertySources.addFirst(mappedPropertySource);

        logger.debug("Added {} mapped properties to environment with high priority", mappedProperties.size());
    }

    /**
     * 检查是否存在 Spring AI 原生配置
     */
    public boolean hasSpringAINativeConfiguration(ConfigurableEnvironment environment) {
        Binder binder = Binder.get(environment);

        // 检查是否存在 spring.ai 配置
        try {
            return binder.bind("spring.ai", Map.class).isBound();
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 获取配置优先级信息
     */
    public String getConfigurationPriorityInfo(ConfigurableEnvironment environment) {
        StringBuilder info = new StringBuilder();
        info.append("Configuration Priority (High to Low):\n");

        MutablePropertySources propertySources = environment.getPropertySources();
        int index = 1;
        for (org.springframework.core.env.PropertySource<?> propertySource : propertySources) {
            info.append(String.format("%d. %s\n", index++, propertySource.getName()));
            if (index > 10) { // 只显示前10个
                info.append("   ... (more)\n");
                break;
            }
        }

        return info.toString();
    }
}
