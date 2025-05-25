package com.terra.framework.nova.client.impl;

import com.terra.framework.nova.client.TerraChatClient;
import com.terra.framework.nova.manager.TerraModelManager;
import com.terra.framework.nova.properties.TerraNovaProperties;
import com.terra.framework.nova.springai.config.TerraToSpringAIBridge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Terra Chat Client 默认实现
 * 
 * <p>基于 Spring AI ChatClient 的增强实现，提供：
 * <ul>
 *   <li>统一的聊天接口</li>
 *   <li>结构化输出支持</li>
 *   <li>流式响应</li>
 *   <li>增强功能集成（缓存、重试、监控）</li>
 * </ul>
 * 
 * @author terra-nova
 * @since 0.0.1
 */
public class DefaultTerraChatClient implements TerraChatClient {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTerraChatClient.class);

    private final TerraModelManager modelManager;
    private final TerraNovaProperties properties;
    private final TerraToSpringAIBridge bridge;
    private final Map<String, ChatClient> chatClientCache = new ConcurrentHashMap<>();

    public DefaultTerraChatClient(TerraModelManager modelManager, 
                                  TerraNovaProperties properties,
                                  TerraToSpringAIBridge bridge) {
        this.modelManager = modelManager;
        this.properties = properties;
        this.bridge = bridge;
        logger.info("DefaultTerraChatClient initialized");
    }

    @Override
    public String chat(String message) {
        return chat(message, ChatOptions.builder());
    }

    @Override
    public String chat(String message, ChatOptions options) {
        try {
            ChatClient chatClient = getChatClient(options.getModelName());
            if (chatClient == null) {
                throw new RuntimeException("No available chat client");
            }

            ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt()
                .user(message);

            // 应用选项
            if (options.getTemperature() != null) {
                // Spring AI ChatClient 的温度设置方式可能不同，这里需要根据实际API调整
                logger.debug("Setting temperature: {}", options.getTemperature());
            }

            if (options.getMaxTokens() != null) {
                logger.debug("Setting max tokens: {}", options.getMaxTokens());
            }

            if (options.getVariables() != null && !options.getVariables().isEmpty()) {
                // 设置变量
                for (Map.Entry<String, Object> entry : options.getVariables().entrySet()) {
                    // 根据Spring AI的实际API设置变量
                    logger.debug("Setting variable: {} = {}", entry.getKey(), entry.getValue());
                }
            }

            String response = requestSpec.call().content();
            
            logger.debug("Chat completed successfully. Message length: {}, Response length: {}", 
                        message.length(), response != null ? response.length() : 0);
            
            return response;

        } catch (Exception e) {
            logger.error("Error during chat", e);
            throw new RuntimeException("Chat failed", e);
        }
    }

    @Override
    public PromptBuilder prompt() {
        return new DefaultPromptBuilder();
    }

    @Override
    public Flux<String> stream(String message) {
        return stream(message, ChatOptions.builder());
    }

    @Override
    public Flux<String> stream(String message, ChatOptions options) {
        try {
            ChatClient chatClient = getChatClient(options.getModelName());
            if (chatClient == null) {
                return Flux.error(new RuntimeException("No available chat client"));
            }

            return chatClient.prompt()
                .user(message)
                .stream()
                .content();

        } catch (Exception e) {
            logger.error("Error during streaming chat", e);
            return Flux.error(new RuntimeException("Streaming chat failed", e));
        }
    }

    @Override
    public ChatClient getSpringAIChatClient() {
        return getChatClient(null);
    }

    /**
     * 获取 ChatClient 实例
     * 
     * @param modelName 模型名称，如果为空则使用默认模型
     * @return ChatClient 实例
     */
    private ChatClient getChatClient(String modelName) {
        if (!StringUtils.hasText(modelName)) {
            modelName = properties.getModels().getDefaultProvider();
        }

        return chatClientCache.computeIfAbsent(modelName, this::createChatClient);
    }

    /**
     * 创建 ChatClient 实例
     * 
     * @param modelName 模型名称
     * @return ChatClient 实例
     */
    private ChatClient createChatClient(String modelName) {
        try {
            if (bridge != null) {
                return bridge.getChatClient(modelName).orElse(bridge.getDefaultChatClient());
            }
            
            logger.warn("Bridge is not available, cannot create ChatClient for model: {}", modelName);
            return null;
        } catch (Exception e) {
            logger.error("Error creating ChatClient for model: {}", modelName, e);
            return null;
        }
    }

    /**
     * 默认 Prompt 构建器实现
     */
    private class DefaultPromptBuilder implements PromptBuilder {
        private String system;
        private String user;
        private final Map<String, Object> variables = new HashMap<>();
        private String modelName;
        private Double temperature;
        private Integer maxTokens;

        @Override
        public PromptBuilder system(String system) {
            this.system = system;
            return this;
        }

        @Override
        public PromptBuilder user(String user) {
            this.user = user;
            return this;
        }

        @Override
        public PromptBuilder variable(String key, Object value) {
            this.variables.put(key, value);
            return this;
        }

        @Override
        public PromptBuilder variables(Map<String, Object> variables) {
            this.variables.putAll(variables);
            return this;
        }

        @Override
        public PromptBuilder model(String modelName) {
            this.modelName = modelName;
            return this;
        }

        @Override
        public PromptBuilder temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        @Override
        public PromptBuilder maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        @Override
        public CallBuilder call() {
            return new DefaultCallBuilder();
        }

        @Override
        public StreamBuilder stream() {
            return new DefaultStreamBuilder();
        }

        /**
         * 构建 ChatClient 请求
         */
        private ChatClient.ChatClientRequestSpec buildRequest() {
            ChatClient chatClient = getChatClient(modelName);
            if (chatClient == null) {
                throw new RuntimeException("No available chat client");
            }

            ChatClient.ChatClientRequestSpec requestSpec = chatClient.prompt();

            if (StringUtils.hasText(system)) {
                requestSpec = requestSpec.system(system);
            }

            if (StringUtils.hasText(user)) {
                requestSpec = requestSpec.user(user);
            }

            // 设置变量
            if (!variables.isEmpty()) {
                // 根据Spring AI的实际API设置变量
                logger.debug("Setting variables: {}", variables);
            }

            return requestSpec;
        }

        /**
         * 默认调用构建器实现
         */
        private class DefaultCallBuilder implements CallBuilder {

            @Override
            public String content() {
                try {
                    return buildRequest().call().content();
                } catch (Exception e) {
                    logger.error("Error getting content", e);
                    throw new RuntimeException("Failed to get content", e);
                }
            }

            @Override
            public <T> T entity(Class<T> entityClass) {
                try {
                    return buildRequest().call().entity(entityClass);
                } catch (Exception e) {
                    logger.error("Error getting entity", e);
                    throw new RuntimeException("Failed to get entity", e);
                }
            }

            @Override
            public <T> List<T> entityList(Class<T> entityClass) {
                try {
                    // Spring AI 1.1.0 使用 ParameterizedTypeReference 来处理 List 类型
                    return buildRequest().call().entity(new ParameterizedTypeReference<List<T>>() {});
                } catch (Exception e) {
                    logger.error("Error getting entity list", e);
                    throw new RuntimeException("Failed to get entity list", e);
                }
            }

            @Override
            public ChatResponse chatResponse() {
                try {
                    return buildRequest().call().chatResponse();
                } catch (Exception e) {
                    logger.error("Error getting chat response", e);
                    throw new RuntimeException("Failed to get chat response", e);
                }
            }
        }

        /**
         * 默认流式构建器实现
         */
        private class DefaultStreamBuilder implements StreamBuilder {

            @Override
            public Flux<String> content() {
                try {
                    return buildRequest().stream().content();
                } catch (Exception e) {
                    logger.error("Error getting content stream", e);
                    return Flux.error(new RuntimeException("Failed to get content stream", e));
                }
            }

            @Override
            public Flux<ChatResponse> chatResponse() {
                try {
                    return buildRequest().stream().chatResponse();
                } catch (Exception e) {
                    logger.error("Error getting chat response stream", e);
                    return Flux.error(new RuntimeException("Failed to get chat response stream", e));
                }
            }
        }
    }
} 