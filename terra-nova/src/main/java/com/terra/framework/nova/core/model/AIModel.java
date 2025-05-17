package com.terra.framework.nova.core.model;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

/**
 * 统一AI模型接口
 *
 * @author terra-nova
 */
public interface AIModel {

    /**
     * 生成文本响应
     *
     * @param prompt 提示词
     * @param parameters 模型参数
     * @return 生成的文本响应
     */
    ModelResponse generate(String prompt, Map<String, Object> parameters);

    /**
     * 异步生成文本
     *
     * @param prompt 提示词
     * @param parameters 模型参数
     * @return 异步结果
     */
    CompletableFuture<ModelResponse> generateAsync(String prompt, Map<String, Object> parameters);

    /**
     * 流式生成文本
     *
     * @param prompt 提示词
     * @param parameters 模型参数
     * @return 响应流
     */
    Publisher<String> generateStream(String prompt, Map<String, Object> parameters);

    /**
     * 对话式文本生成
     *
     * @param messages 消息列表
     * @param parameters 模型参数
     * @return 生成的文本响应
     */
    ModelResponse chat(List<Message> messages, Map<String, Object> parameters);

    /**
     * 异步对话式文本生成
     *
     * @param messages 消息列表
     * @param parameters 模型参数
     * @return 异步结果
     */
    CompletableFuture<ModelResponse> chatAsync(List<Message> messages, Map<String, Object> parameters);

    /**
     * 流式对话生成
     *
     * @param messages 消息列表
     * @param parameters 模型参数
     * @return 响应流
     */
    Publisher<String> chatStream(List<Message> messages, Map<String, Object> parameters);

    /**
     * 获取模型信息
     *
     * @return 模型信息
     */
    ModelInfo getModelInfo();

    /**
     * 初始化模型
     */
    void init();

    /**
     * 关闭模型
     */
    void close();
}
