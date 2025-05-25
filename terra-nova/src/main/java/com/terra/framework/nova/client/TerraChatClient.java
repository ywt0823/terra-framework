package com.terra.framework.nova.client;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.Map;

/**
 * Terra Chat Client 接口
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
public interface TerraChatClient {

    /**
     * 简单聊天接口
     * 
     * @param message 用户消息
     * @return AI 响应
     */
    String chat(String message);

    /**
     * 带选项的聊天接口
     * 
     * @param message 用户消息
     * @param options 聊天选项
     * @return AI 响应
     */
    String chat(String message, ChatOptions options);

    /**
     * 获取 Prompt 构建器
     * 
     * @return Prompt 构建器
     */
    PromptBuilder prompt();

    /**
     * 流式聊天接口
     * 
     * @param message 用户消息
     * @return 响应流
     */
    Flux<String> stream(String message);

    /**
     * 流式聊天接口（带选项）
     * 
     * @param message 用户消息
     * @param options 聊天选项
     * @return 响应流
     */
    Flux<String> stream(String message, ChatOptions options);

    /**
     * 获取底层的 Spring AI ChatClient
     * 
     * @return Spring AI ChatClient
     */
    ChatClient getSpringAIChatClient();

    /**
     * Prompt 构建器接口
     */
    interface PromptBuilder {
        
        /**
         * 设置系统消息
         * 
         * @param system 系统消息
         * @return 构建器
         */
        PromptBuilder system(String system);

        /**
         * 设置用户消息
         * 
         * @param user 用户消息
         * @return 构建器
         */
        PromptBuilder user(String user);

        /**
         * 添加变量
         * 
         * @param key 变量名
         * @param value 变量值
         * @return 构建器
         */
        PromptBuilder variable(String key, Object value);

        /**
         * 添加多个变量
         * 
         * @param variables 变量映射
         * @return 构建器
         */
        PromptBuilder variables(Map<String, Object> variables);

        /**
         * 设置模型名称
         * 
         * @param modelName 模型名称
         * @return 构建器
         */
        PromptBuilder model(String modelName);

        /**
         * 设置温度参数
         * 
         * @param temperature 温度值
         * @return 构建器
         */
        PromptBuilder temperature(Double temperature);

        /**
         * 设置最大 Token 数
         * 
         * @param maxTokens 最大 Token 数
         * @return 构建器
         */
        PromptBuilder maxTokens(Integer maxTokens);

        /**
         * 调用并返回内容
         * 
         * @return 响应内容
         */
        CallBuilder call();

        /**
         * 流式调用
         * 
         * @return 流式构建器
         */
        StreamBuilder stream();
    }

    /**
     * 调用构建器接口
     */
    interface CallBuilder {
        
        /**
         * 获取响应内容
         * 
         * @return 响应内容
         */
        String content();

        /**
         * 获取结构化实体
         * 
         * @param entityClass 实体类
         * @param <T> 实体类型
         * @return 实体实例
         */
        <T> T entity(Class<T> entityClass);

        /**
         * 获取实体列表
         * 
         * @param entityClass 实体类
         * @param <T> 实体类型
         * @return 实体列表
         */
        <T> List<T> entityList(Class<T> entityClass);

        /**
         * 获取完整的聊天响应
         * 
         * @return 聊天响应
         */
        ChatResponse chatResponse();
    }

    /**
     * 流式构建器接口
     */
    interface StreamBuilder {
        
        /**
         * 获取内容流
         * 
         * @return 内容流
         */
        Flux<String> content();

        /**
         * 获取聊天响应流
         * 
         * @return 聊天响应流
         */
        Flux<ChatResponse> chatResponse();
    }

    /**
     * 聊天选项
     */
    class ChatOptions {
        private String modelName;
        private Double temperature;
        private Integer maxTokens;
        private Map<String, Object> variables;
        private boolean enableCache = true;
        private boolean enableRetry = true;
        private boolean enableMonitoring = true;

        // Getters and Setters
        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public Double getTemperature() {
            return temperature;
        }

        public void setTemperature(Double temperature) {
            this.temperature = temperature;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public void setMaxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
        }

        public Map<String, Object> getVariables() {
            return variables;
        }

        public void setVariables(Map<String, Object> variables) {
            this.variables = variables;
        }

        public boolean isEnableCache() {
            return enableCache;
        }

        public void setEnableCache(boolean enableCache) {
            this.enableCache = enableCache;
        }

        public boolean isEnableRetry() {
            return enableRetry;
        }

        public void setEnableRetry(boolean enableRetry) {
            this.enableRetry = enableRetry;
        }

        public boolean isEnableMonitoring() {
            return enableMonitoring;
        }

        public void setEnableMonitoring(boolean enableMonitoring) {
            this.enableMonitoring = enableMonitoring;
        }

        // Builder pattern
        public static ChatOptions builder() {
            return new ChatOptions();
        }

        public ChatOptions modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public ChatOptions temperature(Double temperature) {
            this.temperature = temperature;
            return this;
        }

        public ChatOptions maxTokens(Integer maxTokens) {
            this.maxTokens = maxTokens;
            return this;
        }

        public ChatOptions variables(Map<String, Object> variables) {
            this.variables = variables;
            return this;
        }

        public ChatOptions enableCache(boolean enableCache) {
            this.enableCache = enableCache;
            return this;
        }

        public ChatOptions enableRetry(boolean enableRetry) {
            this.enableRetry = enableRetry;
            return this;
        }

        public ChatOptions enableMonitoring(boolean enableMonitoring) {
            this.enableMonitoring = enableMonitoring;
            return this;
        }
    }
} 