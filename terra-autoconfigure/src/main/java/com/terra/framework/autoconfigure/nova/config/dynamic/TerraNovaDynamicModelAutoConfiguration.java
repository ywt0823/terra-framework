package com.terra.framework.autoconfigure.nova.config.dynamic;

import com.terra.framework.autoconfigure.nova.config.deepseek.TerraDeepSeekAutoConfiguration;
import com.terra.framework.autoconfigure.nova.config.openai.TerraOpenaiAutoConfiguration;
import com.terra.framework.autoconfigure.nova.properties.TerraAiDynamicModelProperties;
import com.terra.framework.nova.core.DynamicChatModel;
import com.terra.framework.nova.core.DynamicEmbeddingModel;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.util.HashMap;
import java.util.Map;

/**
 * Terra Nova 动态模型选择自动配置。
 * <p>
 * 当启用时，此配置会创建 {@link DynamicChatModel} 和 {@link DynamicEmbeddingModel}
 * 作为 {@link Primary} 的 Bean。它们会代理所有对 {@link ChatClient} 和 {@link EmbeddingModel}
 * 的调用，并根据 {@link com.terra.framework.nova.core.ModelProviderContextHolder} 的设置动态路由到正确的模型实例。
 *
 * @author DeavyJones
 * @version 1.0.0
 * @since 1.0.0
 */
@ConditionalOnProperty(prefix = "terra.ai.dynamic.model", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({TerraDeepSeekAutoConfiguration.class, TerraOpenaiAutoConfiguration.class})
@EnableConfigurationProperties(TerraAiDynamicModelProperties.class)
public class TerraNovaDynamicModelAutoConfiguration {

    private static final String DEFAULT_CHAT_CLIENT = "openAiChatModel";
    private static final String DEFAULT_EMBEDDING_CLIENT = "openAiEmbeddingModel";

    @Bean
    @Primary
    @ConditionalOnBean(ChatModel.class)
    public DynamicChatModel dynamicChatClient(ConfigurableListableBeanFactory beanFactory) {
        Map<String, ChatModel> chatClients = beanFactory.getBeansOfType(ChatModel.class);
        Map<String, ChatModel> clientMap = new HashMap<>();
        chatClients.forEach((name, client) -> {
            // 排除自己，防止循环引用
            if (!(client instanceof DynamicChatModel)) {
                clientMap.put(name, client);
            }
        });
        // 检查默认客户端是否存在
        if (!clientMap.containsKey(DEFAULT_CHAT_CLIENT)) {
            // 如果默认客户端不存在，则选择第一个可用的客户端作为默认
            return new DynamicChatModel(clientMap, clientMap.keySet().stream().findFirst().orElseThrow());
        }
        return new DynamicChatModel(clientMap, DEFAULT_CHAT_CLIENT);
    }

    @Bean
    @Primary
    @ConditionalOnBean(EmbeddingModel.class)
    public DynamicEmbeddingModel dynamicEmbeddingClient(ConfigurableListableBeanFactory beanFactory) {
        Map<String, EmbeddingModel> embeddingClients = beanFactory.getBeansOfType(EmbeddingModel.class);
        Map<String, EmbeddingModel> clientMap = new HashMap<>();
        embeddingClients.forEach((name, client) -> {
            if (!(client instanceof DynamicEmbeddingModel)) {
                clientMap.put(name, client);
            }
        });
        // 检查默认客户端是否存在
        if (!clientMap.containsKey(DEFAULT_EMBEDDING_CLIENT)) {
            // 如果默认客户端不存在，则选择第一个可用的客户端作为默认
            return new DynamicEmbeddingModel(clientMap, clientMap.keySet().stream().findFirst().orElseThrow());
        }
        return new DynamicEmbeddingModel(clientMap, DEFAULT_EMBEDDING_CLIENT);
    }
}
