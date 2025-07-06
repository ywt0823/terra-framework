package com.terra.framework.nova.core;

import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

/**
 * 动态 {@link ChatModel} 实现。
 * <p>
 * 该客户端作为一个代理，将请求路由到在 {@link ModelProviderContextHolder} 中指定的具体 {@link ChatModel} 实例。
 * 如果上下文中没有指定模型ID，则会使用默认的客户端。
 *
 * @author DeavyJones
 * @version 1.0.0
 * @since 1.0.0
 */
public class DynamicChatModel implements ChatModel {

    private final Map<String, ChatModel> chatModelMap;
    private final String defaultClient;

    /**
     * 构造一个新的 DynamicChatClient。
     *
     * @param chatModelMap  包含所有可用 ChatModel 的映射，键为 Bean 名称，值为模型实例。
     * @param defaultClient 默认的 ChatModel Bean 名称，在未指定模型ID时使用。
     */
    public DynamicChatModel(Map<String, ChatModel> chatModelMap, String defaultClient) {
        Assert.notNull(chatModelMap, "chatModelMap must not be null");
        Assert.notNull(defaultClient, "defaultClient must not be null");
        this.chatModelMap = Collections.unmodifiableMap(chatModelMap);
        this.defaultClient = defaultClient;
    }

    @Override
    public ChatResponse call(Prompt prompt) {
        return getTargetClient().call(prompt);
    }

    /**
     * 根据上下文获取目标 ChatModel。
     *
     * @return 目标 ChatModel 实例。
     * @throws IllegalArgumentException 如果找不到指定的客户端。
     */
    private ChatModel getTargetClient() {
        String modelId = ModelProviderContextHolder.getModelId();
        if (modelId == null) {
            modelId = this.defaultClient;
        }
        ChatModel client = this.chatModelMap.get(modelId);
        if (client == null) {
            throw new IllegalArgumentException(String.format(
                "未找到ID为 '%s' 的 ChatModel Bean。可用ID: %s",
                modelId, this.chatModelMap.keySet()
            ));
        }
        return client;
    }
}
