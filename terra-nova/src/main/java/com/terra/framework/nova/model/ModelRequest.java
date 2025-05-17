package com.terra.framework.nova.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;

/**
 * 模型请求
 *
 * @author terra-nova
 */
@Data
public class ModelRequest {

    /**
     * 请求ID
     */
    private String requestId;

    /**
     * 提示词
     */
    private String prompt;

    /**
     * 消息列表（用于对话）
     */
    private List<Message> messages;

    /**
     * 模型参数
     */
    private Map<String, Object> parameters;

    /**
     * 是否流式请求
     */
    private boolean stream;

    /**
     * 创建文本生成请求
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @param stream 是否流式
     */
    public ModelRequest(String prompt, Map<String, Object> parameters, boolean stream) {
        this.prompt = prompt;
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.stream = stream;
    }

    /**
     * 创建对话请求
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @param stream 是否流式
     */
    public ModelRequest(List<Message> messages, Map<String, Object> parameters, boolean stream) {
        this.messages = messages;
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.stream = stream;
    }

    /**
     * 创建一个请求构建器
     *
     * @return 请求构建器
     */
    public static ModelRequestBuilder builder() {
        return new ModelRequestBuilder();
    }

    /**
     * 模型请求构建器
     */
    public static class ModelRequestBuilder {
        private String prompt;
        private List<Message> messages = new ArrayList<>();
        private Map<String, Object> parameters = new HashMap<>();
        private boolean stream = false;

        /**
         * 设置提示词
         *
         * @param prompt 提示词
         * @return 构建器
         */
        public ModelRequestBuilder withPrompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        /**
         * 添加消息
         *
         * @param message 消息
         * @return 构建器
         */
        public ModelRequestBuilder addMessage(Message message) {
            this.messages.add(message);
            return this;
        }

        /**
         * 添加系统消息
         *
         * @param content 内容
         * @return 构建器
         */
        public ModelRequestBuilder addSystemMessage(String content) {
            this.messages.add(Message.ofSystem(content));
            return this;
        }

        /**
         * 添加用户消息
         *
         * @param content 内容
         * @return 构建器
         */
        public ModelRequestBuilder addUserMessage(String content) {
            this.messages.add(Message.ofUser(content));
            return this;
        }

        /**
         * 添加参数
         *
         * @param key 参数名
         * @param value 参数值
         * @return 构建器
         */
        public ModelRequestBuilder withParameter(String key, Object value) {
            this.parameters.put(key, value);
            return this;
        }

        /**
         * 设置参数
         *
         * @param parameters 参数
         * @return 构建器
         */
        public ModelRequestBuilder withParameters(Map<String, Object> parameters) {
            if (parameters != null) {
                this.parameters.putAll(parameters);
            }
            return this;
        }

        /**
         * 设置是否流式
         *
         * @param stream 是否流式
         * @return 构建器
         */
        public ModelRequestBuilder withStream(boolean stream) {
            this.stream = stream;
            return this;
        }

        /**
         * 构建请求
         *
         * @return 请求对象
         */
        public ModelRequest build() {
            if (prompt != null && !prompt.isEmpty()) {
                return new ModelRequest(prompt, parameters, stream);
            } else if (!messages.isEmpty()) {
                return new ModelRequest(messages, parameters, stream);
            } else {
                throw new IllegalStateException("必须设置提示词或消息列表");
            }
        }
    }
}
