package com.terra.framework.nova.core;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

/**
 * 动态 {@link ChatClient} 实现。
 * <p>
 * 该客户端作为一个代理，将请求路由到在 {@link ModelProviderContextHolder} 中指定的具体 {@link ChatClient} 实例。
 * 如果上下文中没有指定模型ID，则会使用默认的客户端。
 * <p>
 * 使用示例：
 * <pre>{@code
 * // 设置要使用的模型
 * ModelProviderContextHolder.setModelId("openAiChatClient");
 * try {
 *     String response = dynamicChatClient.prompt()
 *         .user("你好")
 *         .call()
 *         .content();
 * } finally {
 *     ModelProviderContextHolder.clear();
 * }
 * }</pre>
 *
 * @author <a href="mailto:love.yu@terra.com">Yu</a>
 * @version 1.0.0
 * @since 1.0.0
 */
public class DynamicChatClient implements ChatClient {

    private final Map<String, ChatClient> chatClientMap;
    private final String defaultClient;

    /**
     * 构造一个新的 DynamicChatClient。
     *
     * @param chatClientMap 包含所有可用 ChatClient 的映射，键为 Bean 名称，值为客户端实例。
     * @param defaultClient 默认的 ChatClient Bean 名称，在未指定模型ID时使用。
     */
    public DynamicChatClient(Map<String, ChatClient> chatClientMap, String defaultClient) {
        Assert.notNull(chatClientMap, "chatClientMap must not be null");
        Assert.notNull(defaultClient, "defaultClient must not be null");
        Assert.isTrue(!chatClientMap.isEmpty(), "chatClientMap must not be empty");
        Assert.isTrue(chatClientMap.containsKey(defaultClient),
            String.format("默认客户端 '%s' 在 chatClientMap 中不存在", defaultClient));

        this.chatClientMap = Collections.unmodifiableMap(chatClientMap);
        this.defaultClient = defaultClient;
    }

    @Override
    public ChatClient.ChatClientRequestSpec prompt() {
        return getTargetClient().prompt();
    }

    @Override
    public ChatClient.ChatClientRequestSpec prompt(String content) {
        return getTargetClient().prompt(content);
    }

    @Override
    public ChatClient.ChatClientRequestSpec prompt(Prompt prompt) {
        return getTargetClient().prompt(prompt);
    }

    @Override
    public ChatClient.Builder mutate() {
        return getTargetClient().mutate();
    }

    /**
     * 根据上下文获取目标 ChatClient。
     *
     * @return 目标 ChatClient 实例。
     * @throws IllegalArgumentException 如果找不到指定的客户端。
     */
    private ChatClient getTargetClient() {
        String modelId = ModelProviderContextHolder.getModelId();
        if (modelId == null) {
            modelId = this.defaultClient;
        }
        ChatClient client = this.chatClientMap.get(modelId);
        if (client == null) {
            throw new IllegalArgumentException(String.format(
                "未找到ID为 '%s' 的 ChatClient Bean。可用ID: %s",
                modelId, this.chatClientMap.keySet()
            ));
        }
        return client;
    }

    /**
     * 获取所有可用的 ChatClient ID。
     *
     * @return 所有可用的客户端ID集合
     */
    public java.util.Set<String> getAvailableClientIds() {
        return this.chatClientMap.keySet();
    }

    /**
     * 获取默认的 ChatClient ID。
     *
     * @return 默认客户端ID
     */
    public String getDefaultClientId() {
        return this.defaultClient;
    }

    /**
     * 检查指定ID的 ChatClient 是否存在。
     *
     * @param clientId 客户端ID
     * @return 如果存在返回 true，否则返回 false
     */
    public boolean hasClient(String clientId) {
        return this.chatClientMap.containsKey(clientId);
    }

    /**
     * 获取当前活动的 ChatClient ID。
     * <p>
     * 如果上下文中没有设置模型ID，则返回默认客户端ID。
     *
     * @return 当前活动的客户端ID
     */
    public String getCurrentClientId() {
        String modelId = ModelProviderContextHolder.getModelId();
        return modelId != null ? modelId : this.defaultClient;
    }
}
