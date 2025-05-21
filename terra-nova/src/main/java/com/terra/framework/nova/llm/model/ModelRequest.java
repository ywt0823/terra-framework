package com.terra.framework.nova.llm.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
     * 工具列表，提供给模型可调用的工具
     */
    private List<ToolDefinition> tools;
    
    /**
     * 工具选择策略，控制模型如何选择工具
     * 可能的值：
     * - "auto"：模型自行决定是否调用工具
     * - "none"：模型不调用任何工具
     * - {"type": "function", "function": {"name": "function_name"}}：指定调用特定函数
     */
    private Object toolChoice;

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
     * @param prompt     提示词
     * @param parameters 参数
     * @param stream     是否流式
     */
    public ModelRequest(String prompt, Map<String, Object> parameters, boolean stream) {
        this(prompt, parameters, stream, null, null);
    }

    /**
     * 创建对话请求
     *
     * @param messages   消息列表
     * @param parameters 参数
     * @param stream     是否流式
     */
    public ModelRequest(List<Message> messages, Map<String, Object> parameters, boolean stream) {
        this(messages, parameters, stream, null, null);
    }
    
    /**
     * 创建文本生成请求（带工具支持）
     *
     * @param prompt     提示词
     * @param parameters 参数
     * @param stream     是否流式
     * @param tools      可供调用的工具列表
     * @param toolChoice 工具选择策略
     */
    public ModelRequest(String prompt, Map<String, Object> parameters, boolean stream, 
                         List<ToolDefinition> tools, Object toolChoice) {
        this.prompt = prompt;
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.stream = stream;
        this.tools = tools;
        this.toolChoice = toolChoice;
    }

    /**
     * 创建对话请求（带工具支持）
     *
     * @param messages   消息列表
     * @param parameters 参数
     * @param stream     是否流式
     * @param tools      可供调用的工具列表
     * @param toolChoice 工具选择策略
     */
    public ModelRequest(List<Message> messages, Map<String, Object> parameters, boolean stream,
                         List<ToolDefinition> tools, Object toolChoice) {
        this.messages = messages;
        this.parameters = parameters != null ? parameters : new HashMap<>();
        this.stream = stream;
        this.tools = tools;
        this.toolChoice = toolChoice;
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
        private List<ToolDefinition> tools;
        private Object toolChoice;

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
         * 添加消息
         *
         * @param messages 消息
         * @return 构建器
         */
        public ModelRequestBuilder addMessages(List<Message> messages) {
            this.messages.addAll(messages);
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
         * @param key   参数名
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
         * 设置工具列表
         *
         * @param tools 工具列表
         * @return 构建器
         */
        public ModelRequestBuilder withTools(List<ToolDefinition> tools) {
            this.tools = tools;
            return this;
        }
        
        /**
         * 添加单个工具
         *
         * @param tool 工具定义
         * @return 构建器
         */
        public ModelRequestBuilder addTool(ToolDefinition tool) {
            if (this.tools == null) {
                this.tools = new ArrayList<>();
            }
            this.tools.add(tool);
            return this;
        }
        
        /**
         * 设置工具选择策略
         *
         * @param toolChoice 工具选择策略
         * @return 构建器
         */
        public ModelRequestBuilder withToolChoice(Object toolChoice) {
            this.toolChoice = toolChoice;
            return this;
        }
        
        /**
         * 设置自动工具选择策略
         *
         * @return 构建器
         */
        public ModelRequestBuilder withAutoToolChoice() {
            this.toolChoice = "auto";
            return this;
        }
        
        /**
         * 设置不使用工具的策略
         *
         * @return 构建器
         */
        public ModelRequestBuilder withNoToolChoice() {
            this.toolChoice = "none";
            return this;
        }
        
        /**
         * 设置强制使用特定函数的策略
         *
         * @param functionName 要调用的函数名
         * @return 构建器
         */
        public ModelRequestBuilder withForcedFunctionCall(String functionName) {
            Map<String, Object> function = new HashMap<>();
            function.put("name", functionName);
            
            Map<String, Object> choice = new HashMap<>();
            choice.put("type", "function");
            choice.put("function", function);
            
            this.toolChoice = choice;
            return this;
        }

        /**
         * 构建请求
         *
         * @return 请求对象
         */
        public ModelRequest build() {
            if (prompt != null && !prompt.isEmpty()) {
                return new ModelRequest(prompt, parameters, stream, tools, toolChoice);
            } else if (!messages.isEmpty()) {
                return new ModelRequest(messages, parameters, stream, tools, toolChoice);
            } else {
                throw new IllegalStateException("必须设置提示词或消息列表");
            }
        }
    }
}
