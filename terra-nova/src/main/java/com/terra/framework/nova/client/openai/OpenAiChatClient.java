package com.terra.framework.nova.client.openai;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.util.Assert;

import java.util.List;

/**
 * OpenAI ChatClient 实现。
 * <p>
 * 提供针对 OpenAI 模型的专门化客户端实现，支持 OpenAI 特有的配置选项和功能。
 * 这个类实现了 ChatClient 接口，并提供了便捷的 OpenAI 特有的方法。
 *
 * @author <a href="mailto:love.yu@terra.com">Yu</a>
 * @version 1.0.0
 * @since 1.0.0
 */
public class OpenAiChatClient implements ChatClient {

    private final OpenAiChatModel chatModel;
    private final OpenAiChatOptions defaultOptions;
    private final ChatClient delegateClient;

    /**
     * 构造一个新的 OpenAiChatClient。
     *
     * @param chatModel 底层的 ChatModel 实例
     */
    public OpenAiChatClient(OpenAiChatModel chatModel) {
        this(chatModel, OpenAiChatOptions.builder()
            .model(OpenAiApi.ChatModel.GPT_4_O.getValue())
            .temperature(0.7)
            .build());
    }

    /**
     * 构造一个新的 OpenAiChatClient。
     *
     * @param chatModel      底层的 ChatModel 实例
     * @param defaultOptions 默认的 OpenAI 配置选项
     */
    public OpenAiChatClient(OpenAiChatModel chatModel, OpenAiChatOptions defaultOptions) {
        Assert.notNull(chatModel, "ChatModel must not be null");
        Assert.notNull(defaultOptions, "DefaultOptions must not be null");
        this.chatModel = chatModel;
        this.defaultOptions = defaultOptions;
        this.delegateClient = ChatClient.builder(chatModel).build();
    }

    /**
     * 调用 OpenAI 模型进行对话。
     *
     * @param prompt 提示信息
     * @return 聊天响应
     */
    public ChatResponse call(Prompt prompt) {
        return this.chatModel.call(prompt);
    }

    /**
     * 使用简单的文本消息调用 OpenAI 模型。
     *
     * @param message 用户消息
     * @return 聊天响应
     */
    public ChatResponse call(String message) {
        Prompt prompt = new Prompt(message, defaultOptions);
        return call(prompt);
    }

    /**
     * 使用系统消息和用户消息调用 OpenAI 模型。
     *
     * @param systemMessage 系统消息
     * @param userMessage   用户消息
     * @return 聊天响应
     */
    public ChatResponse call(String systemMessage, String userMessage) {
        Prompt prompt = new Prompt(List.of(
            new SystemMessage(systemMessage),
            new UserMessage(userMessage)
        ), defaultOptions);
        return call(prompt);
    }

    /**
     * 使用自定义选项调用 OpenAI 模型。
     *
     * @param message 用户消息
     * @param options OpenAI 特有的选项
     * @return 聊天响应
     */
    public ChatResponse call(String message, OpenAiChatOptions options) {
        Prompt prompt = new Prompt(message, options);
        return call(prompt);
    }

    /**
     * 获取聊天响应的文本内容。
     *
     * @param message 用户消息
     * @return 响应文本内容
     */
    public String getContent(String message) {
        ChatResponse response = call(message);
        return response.getResult().getOutput().getText();
    }

    /**
     * 使用系统消息和用户消息获取响应文本内容。
     *
     * @param systemMessage 系统消息
     * @param userMessage   用户消息
     * @return 响应文本内容
     */
    public String getContent(String systemMessage, String userMessage) {
        ChatResponse response = call(systemMessage, userMessage);
        return response.getResult().getOutput().getText();
    }

    /**
     * 创建一个新的 OpenAiChatClient 实例，使用不同的选项。
     *
     * @param options 新的 OpenAI 配置选项
     * @return 新的 OpenAiChatClient 实例
     */
    public OpenAiChatClient withOptions(OpenAiChatOptions options) {
        return new OpenAiChatClient(this.chatModel, options);
    }

    /**
     * 创建一个新的 OpenAiChatClient 实例，使用不同的温度值。
     *
     * @param temperature 温度值 (0.0 - 2.0)
     * @return 新的 OpenAiChatClient 实例
     */
    public OpenAiChatClient withTemperature(Double temperature) {
        OpenAiChatOptions newOptions = OpenAiChatOptions.builder()
            .model(defaultOptions.getModel())
            .temperature(temperature)
            .build();
        return withOptions(newOptions);
    }

    /**
     * 创建一个新的 OpenAiChatClient 实例，使用不同的模型。
     *
     * @param model 模型名称
     * @return 新的 OpenAiChatClient 实例
     */
    public OpenAiChatClient withModel(String model) {
        OpenAiChatOptions newOptions = OpenAiChatOptions.builder()
            .model(model)
            .temperature(defaultOptions.getTemperature())
            .build();
        return withOptions(newOptions);
    }

    /**
     * 获取当前使用的模型名称。
     *
     * @return 模型名称
     */
    public String getModelName() {
        return defaultOptions.getModel();
    }

    /**
     * 获取默认配置选项。
     *
     * @return 默认的 OpenAI 配置选项
     */
    public OpenAiChatOptions getDefaultOptions() {
        return defaultOptions;
    }

    /**
     * 获取底层的 ChatModel 实例。
     *
     * @return ChatModel 实例
     */
    public OpenAiChatModel getChatModel() {
        return chatModel;
    }

    // ========== ChatClient 接口实现 ==========

    @Override
    public ChatClient.ChatClientRequestSpec prompt() {
        return delegateClient.prompt();
    }

    @Override
    public ChatClient.ChatClientRequestSpec prompt(String content) {
        return delegateClient.prompt(content);
    }

    @Override
    public ChatClient.ChatClientRequestSpec prompt(Prompt prompt) {
        return delegateClient.prompt(prompt);
    }

    @Override
    public ChatClient.Builder mutate() {
        return delegateClient.mutate();
    }
}
