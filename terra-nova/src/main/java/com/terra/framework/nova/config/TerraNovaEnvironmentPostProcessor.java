package com.terra.framework.nova.config;

import com.terra.framework.nova.properties.TerraNovaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;
import org.springframework.core.env.MutablePropertySources;

import java.util.HashMap;
import java.util.Map;

/**
 * Terra Nova 环境后处理器
 * 
 * <p>在 Spring Boot 启动早期阶段处理配置映射，确保：
 * <ul>
 *   <li>Terra Nova 配置能够自动映射到 Spring AI 配置</li>
 *   <li>配置映射在 Spring AI 自动配置之前完成</li>
 *   <li>支持配置优先级管理</li>
 * </ul>
 * 
 * @author terra-nova
 * @since 0.0.1
 */
public class TerraNovaEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    private static final Logger logger = LoggerFactory.getLogger(TerraNovaEnvironmentPostProcessor.class);
    
    private static final String TERRA_NOVA_MAPPED_PROPERTIES = "terraNovaMappedProperties";
    
    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        try {
            // 绑定 Terra Nova 配置
            Binder binder = Binder.get(environment);
            TerraNovaProperties properties = binder.bind("terra.nova", TerraNovaProperties.class)
                .orElse(new TerraNovaProperties());
            
            // 检查是否启用了 Terra Nova
            if (!properties.isEnabled()) {
                logger.debug("Terra Nova is disabled, skipping configuration mapping");
                return;
            }
            
            // 检查是否启用了 Spring AI 集成
            if (!properties.getSpringAi().isEnabled()) {
                logger.debug("Spring AI integration is disabled, skipping configuration mapping");
                return;
            }
            
            // 执行配置映射
            mapTerraNovaToSpringAI(environment, properties);
            
        } catch (Exception e) {
            logger.warn("Failed to process Terra Nova configuration mapping", e);
        }
    }

    /**
     * 将 Terra Nova 配置映射到 Spring AI 配置
     */
    private void mapTerraNovaToSpringAI(ConfigurableEnvironment environment, TerraNovaProperties properties) {
        logger.info("Processing Terra Nova to Spring AI configuration mapping");
        
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
            
            if (logger.isDebugEnabled()) {
                mappedProperties.forEach((key, value) -> 
                    logger.debug("Mapped: {} = {}", key, value));
            }
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
                case "gemini":
                    mapGeminiProvider(mappedProperties, provider);
                    break;
                case "deepseek":
                    mapDeepSeekProvider(mappedProperties, provider);
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
        mapChatModelOptions(mappedProperties, "spring.ai.openai.chat.options", provider);
        
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
        mapChatModelOptions(mappedProperties, "spring.ai.anthropic.chat.options", provider);
        
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
        mapChatModelOptions(mappedProperties, "spring.ai.ollama.chat.options", provider);
        
        logger.debug("Mapped Ollama provider configuration");
    }

    /**
     * 映射 Gemini 提供商配置
     */
    private void mapGeminiProvider(Map<String, Object> mappedProperties, TerraNovaProperties.Models.Provider provider) {
        if (provider.getApiKey() != null) {
            mappedProperties.put("spring.ai.vertex.ai.gemini.api-key", provider.getApiKey());
        }
        
        // 映射默认聊天模型配置
        mapChatModelOptions(mappedProperties, "spring.ai.vertex.ai.gemini.chat.options", provider);
        
        logger.debug("Mapped Gemini provider configuration");
    }

    /**
     * 映射 DeepSeek 提供商配置
     */
    private void mapDeepSeekProvider(Map<String, Object> mappedProperties, TerraNovaProperties.Models.Provider provider) {
        // DeepSeek 使用自定义配置，不直接映射到 Spring AI
        // 这里可以设置一些标识，让后续的配置知道 DeepSeek 已配置
        if (provider.getApiKey() != null) {
            mappedProperties.put("terra.nova.deepseek.api-key", provider.getApiKey());
        }
        
        if (provider.getBaseUrl() != null) {
            mappedProperties.put("terra.nova.deepseek.base-url", provider.getBaseUrl());
        }
        
        logger.debug("Mapped DeepSeek provider configuration");
    }

    /**
     * 映射聊天模型选项
     */
    private void mapChatModelOptions(Map<String, Object> mappedProperties, String prefix, 
                                   TerraNovaProperties.Models.Provider provider) {
        if (provider.getModels() != null && !provider.getModels().isEmpty()) {
            TerraNovaProperties.Models.Model defaultChatModel = provider.getModels().stream()
                .filter(model -> model.getType() == TerraNovaProperties.Models.ModelType.CHAT)
                .findFirst()
                .orElse(provider.getModels().get(0));
            
            if (defaultChatModel != null) {
                mappedProperties.put(prefix + ".model", defaultChatModel.getName());
                if (defaultChatModel.getTemperature() != null) {
                    mappedProperties.put(prefix + ".temperature", defaultChatModel.getTemperature());
                }
                if (defaultChatModel.getMaxTokens() != null) {
                    mappedProperties.put(prefix + ".max-tokens", defaultChatModel.getMaxTokens());
                }
            }
        }
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
        if (propertySources.contains(TERRA_NOVA_MAPPED_PROPERTIES)) {
            propertySources.remove(TERRA_NOVA_MAPPED_PROPERTIES);
        }
        
        // 添加新的映射属性源，优先级较高但低于命令行参数和系统属性
        MapPropertySource mappedPropertySource = new MapPropertySource(TERRA_NOVA_MAPPED_PROPERTIES, mappedProperties);
        
        // 在 application properties 之前添加，但在系统属性之后
        if (propertySources.contains("applicationConfig: [classpath:/application.yml]")) {
            propertySources.addBefore("applicationConfig: [classpath:/application.yml]", mappedPropertySource);
        } else if (propertySources.contains("applicationConfig: [classpath:/application.properties]")) {
            propertySources.addBefore("applicationConfig: [classpath:/application.properties]", mappedPropertySource);
        } else {
            // 如果找不到标准的 application 配置，就添加到最前面
            propertySources.addFirst(mappedPropertySource);
        }
        
        logger.debug("Added {} mapped properties to environment", mappedProperties.size());
    }

    @Override
    public int getOrder() {
        // 确保在其他环境后处理器之前运行，但在配置文件处理器之后
        return Ordered.HIGHEST_PRECEDENCE + 10;
    }
} 