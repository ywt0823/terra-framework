package com.terra.framework.nova.llm.service;

import com.terra.framework.nova.llm.model.AIModel;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.ModelResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

/**
 * AI服务接口
 *
 * @author terra-nova
 */
public interface AIService {

    /**
     * 生成文本
     *
     * @param prompt 提示词
     * @return 生成的文本内容
     */
    String generateText(String prompt);

    /**
     * 生成文本
     *
     * @param prompt 提示词
     * @return 生成的文本内容
     */
    String generateText(String prompt, String modelId);

    /**
     * 带参数生成文本
     *
     * @param prompt     提示词
     * @param parameters 参数
     * @return 生成的文本内容
     */
    String generateText(String prompt, Map<String, Object> parameters);

    /**
     * 带参数生成文本
     *
     * @param prompt     提示词
     * @param modelId    模型id
     * @param parameters 参数
     * @return 生成的文本内容
     */
    String generateText(String prompt, String modelId, Map<String, Object> parameters);

    /**
     * 异步生成文本
     *
     * @param prompt 提示词
     * @return 异步结果
     */
    CompletableFuture<String> generateTextAsync(String prompt);

    /**
     * 带参数异步生成文本
     *
     * @param prompt     提示词
     * @param parameters 参数
     * @return 异步结果
     */
    CompletableFuture<String> generateTextAsync(String prompt, Map<String, Object> parameters);

    /**
     * 流式生成文本
     *
     * @param prompt 提示词
     * @return 响应流
     */
    Publisher<String> generateTextStream(String prompt);

    /**
     * 带参数流式生成文本
     *
     * @param prompt     提示词
     * @param parameters 参数
     * @return 响应流
     */
    Publisher<String> generateTextStream(String prompt, Map<String, Object> parameters);

    /**
     * 对话生成
     *
     * @param messages 消息列表
     * @return 生成的响应
     */
    String chat(List<Message> messages);

    /**
     * 带参数对话生成
     *
     * @param messages   消息列表
     * @param parameters 参数
     * @return 生成的响应
     */
    String chat(List<Message> messages, Map<String, Object> parameters);

    /**
     * 带参数对话生成
     *
     * @param messages   消息列表
     * @param parameters 参数
     * @return 生成的响应
     */
    String chat(List<Message> messages, String modelId, Map<String, Object> parameters);


    /**
     * 异步对话生成
     *
     * @param messages 消息列表
     * @return 异步结果
     */
    CompletableFuture<String> chatAsync(List<Message> messages);

    /**
     * 带参数异步对话生成
     *
     * @param messages   消息列表
     * @param parameters 参数
     * @return 异步结果
     */
    CompletableFuture<String> chatAsync(List<Message> messages, Map<String, Object> parameters);

    /**
     * 流式对话生成
     *
     * @param messages 消息列表
     * @return 响应流
     */
    Publisher<String> chatStream(List<Message> messages);

    /**
     * 带参数流式对话生成
     *
     * @param messages   消息列表
     * @param parameters 参数
     * @return 响应流
     */
    Publisher<String> chatStream(List<Message> messages, Map<String, Object> parameters);

    /**
     * 获取详细响应
     *
     * @param prompt     提示词
     * @param parameters 参数
     * @return 详细响应
     */
    ModelResponse generateResponse(String prompt, Map<String, Object> parameters);

    ModelResponse generateResponse(String prompt, String modelId, Map<String, Object> parameters);

    /**
     * 获取对话详细响应
     *
     * @param messages   消息列表
     * @param parameters 参数
     * @return 详细响应
     */
    ModelResponse chatResponse(List<Message> messages, Map<String, Object> parameters);

    ModelResponse chatResponse(List<Message> messages, String modelId, Map<String, Object> parameters);

    /**
     * 获取指定模型
     *
     * @param modelId 模型ID
     * @return 模型实例
     */
    AIModel getModel(String modelId);

    /**
     * 获取模型实例
     *
     * @param vendor  厂商
     * @param modelId 模型ID
     * @return 模型实例
     */
    AIModel getModel(String vendor, String modelId);
}
