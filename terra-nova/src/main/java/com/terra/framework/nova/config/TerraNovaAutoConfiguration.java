package com.terra.framework.nova.config;

import com.terra.framework.nova.client.TerraChatClient;
import com.terra.framework.nova.client.impl.DefaultTerraChatClient;
import com.terra.framework.nova.manager.TerraModelManager;
import com.terra.framework.nova.manager.impl.DefaultTerraModelManager;
import com.terra.framework.nova.properties.TerraNovaProperties;
import com.terra.framework.nova.service.TerraRAGService;
import com.terra.framework.nova.service.impl.DefaultTerraRAGService;
import com.terra.framework.nova.springai.config.SpringAIIntegrationManager;
import com.terra.framework.nova.springai.config.TerraToSpringAIBridge;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.core.env.ConfigurableEnvironment;

/**
 * Terra-Nova 自动配置类
 *
 * <p>提供 Terra Framework 与 Spring AI 的集成配置，包括：
 * <ul>
 *   <li>模型管理器配置</li>
 *   <li>聊天客户端配置</li>
 *   <li>RAG 服务配置</li>
 *   <li>Spring AI 桥接配置</li>
 * </ul>
 *
 * @author terra-nova
 * @since 0.0.1
 */
@ConditionalOnProperty(prefix = "terra.nova", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(TerraNovaProperties.class)
@ConditionalOnClass({ChatModel.class, ChatClient.class})
@Import({
        TerraNovaAutoConfiguration.SpringAIIntegrationConfiguration.class,
        TerraNovaAutoConfiguration.TerraNovaMetricsConfiguration.class,
        TerraNovaAutoConfiguration.TerraNovaSecurityConfiguration.class
})
public class TerraNovaAutoConfiguration {

    /**
     * 配置 Terra 模型管理器
     */
    @Bean
    @ConditionalOnMissingBean
    public TerraModelManager terraModelManager(TerraNovaProperties properties) {
        return new DefaultTerraModelManager(properties);
    }

    /**
     * 配置 Terra 到 Spring AI 的桥接器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "terra.nova.spring-ai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public TerraToSpringAIBridge terraToSpringAIBridge(TerraModelManager modelManager) {
        return new TerraToSpringAIBridge(modelManager);
    }

    /**
     * 配置 Spring AI 集成管理器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "terra.nova.spring-ai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SpringAIIntegrationManager springAIIntegrationManager(
            TerraNovaProperties properties,
            TerraToSpringAIBridge bridge) {
        return new SpringAIIntegrationManager(properties, bridge);
    }

    /**
     * 配置 Spring AI 配置映射器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "terra.nova.spring-ai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public SpringAIConfigurationMapper springAIConfigurationMapper() {
        return new SpringAIConfigurationMapper();
    }

    /**
     * 配置 Terra 聊天客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public TerraChatClient terraChatClient(
            TerraModelManager modelManager,
            TerraNovaProperties properties,
            TerraToSpringAIBridge bridge) {
        return new DefaultTerraChatClient(modelManager, properties, bridge);
    }

    /**
     * 配置 Terra RAG 服务
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(VectorStore.class)
    @ConditionalOnProperty(prefix = "terra.nova.rag", name = "enabled", havingValue = "true")
    public TerraRAGService terraRAGService(
            TerraChatClient chatClient,
            VectorStore vectorStore,
            TerraNovaProperties properties) {
        return new DefaultTerraRAGService(chatClient, vectorStore, properties);
    }

    /**
     * Spring AI 集成配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "terra.nova.spring-ai", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class SpringAIIntegrationConfiguration {
        // Spring AI 相关的额外配置将在这里添加
    }

    /**
     * 指标监控配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "terra.nova.enhancement.monitoring", name = "enabled", havingValue = "true", matchIfMissing = true)
    static class TerraNovaMetricsConfiguration {
        // 监控指标配置将在这里添加
    }

    /**
     * 安全配置
     */
    @Configuration
    @ConditionalOnProperty(prefix = "terra.nova.security", name = "enabled", havingValue = "true")
    static class TerraNovaSecurityConfiguration {
        // 安全相关配置将在这里添加
    }
}
