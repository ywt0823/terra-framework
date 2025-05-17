package com.terra.framework.nova.core.service;

import com.terra.framework.nova.core.blend.ModelBlender;
import com.terra.framework.nova.core.model.Message;
import com.terra.framework.nova.core.model.ModelResponse;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

/**
 * 增强型AI服务接口，扩展标准AIService并添加模型混合功能
 *
 * @author terra-nova
 */
public interface EnhancedAIService extends AIService {

    /**
     * 获取模型混合器服务
     *
     * @return 模型混合器服务
     */
    BlenderService getBlenderService();

    /**
     * 获取当前使用的模型混合器
     *
     * @return 模型混合器
     */
    ModelBlender getModelBlender();

    /**
     * 使用混合模式生成文本
     *
     * @param prompt 提示词
     * @return 混合生成的文本结果
     */
    String generateTextWithBlending(String prompt);

    /**
     * 使用混合模式生成文本
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @return 混合生成的文本结果
     */
    String generateTextWithBlending(String prompt, Map<String, Object> parameters);

    /**
     * 使用混合模式异步生成文本
     *
     * @param prompt 提示词
     * @return 混合生成的文本异步结果
     */
    CompletableFuture<String> generateTextWithBlendingAsync(String prompt);

    /**
     * 使用混合模式异步生成文本
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @return 混合生成的文本异步结果
     */
    CompletableFuture<String> generateTextWithBlendingAsync(String prompt, Map<String, Object> parameters);

    /**
     * 使用混合模式进行对话
     *
     * @param messages 消息列表
     * @return 混合生成的对话结果
     */
    String chatWithBlending(List<Message> messages);

    /**
     * 使用混合模式进行对话
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @return 混合生成的对话结果
     */
    String chatWithBlending(List<Message> messages, Map<String, Object> parameters);

    /**
     * 使用混合模式异步进行对话
     *
     * @param messages 消息列表
     * @return 混合生成的对话异步结果
     */
    CompletableFuture<String> chatWithBlendingAsync(List<Message> messages);

    /**
     * 使用混合模式异步进行对话
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @return 混合生成的对话异步结果
     */
    CompletableFuture<String> chatWithBlendingAsync(List<Message> messages, Map<String, Object> parameters);

    /**
     * 获取混合生成的详细响应
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @return 混合生成的详细响应
     */
    ModelResponse generateResponseWithBlending(String prompt, Map<String, Object> parameters);

    /**
     * 获取混合对话的详细响应
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @return 混合生成的详细响应
     */
    ModelResponse chatResponseWithBlending(List<Message> messages, Map<String, Object> parameters);

    /**
     * 添加模型到混合器
     *
     * @param modelId 模型ID
     * @return 是否添加成功
     */
    boolean addModelToBlender(String modelId);

    /**
     * 添加模型到混合器
     *
     * @param modelId 模型ID
     * @param weight 权重
     * @return 是否添加成功
     */
    boolean addModelToBlender(String modelId, int weight);

    /**
     * 添加多个模型到混合器
     *
     * @param modelIds 模型ID列表
     */
    void addModelsToBlender(List<String> modelIds);

    /**
     * 从混合器移除模型
     *
     * @param modelId 模型ID
     */
    void removeModelFromBlender(String modelId);

    /**
     * 获取当前混合器中的所有模型ID
     *
     * @return 模型ID列表
     */
    List<String> getBlenderModelIds();

    /**
     * 流式生成混合文本
     *
     * @param prompt 提示词
     * @return 响应流
     */
    Publisher<String> generateBlendingTextStream(String prompt);

    /**
     * 带参数流式生成混合文本
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @return 响应流
     */
    Publisher<String> generateBlendingTextStream(String prompt, Map<String, Object> parameters);

    /**
     * 流式混合对话
     *
     * @param messages 消息列表
     * @return 响应流
     */
    Publisher<String> chatBlendingStream(List<Message> messages);

    /**
     * 带参数流式混合对话
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @return 响应流
     */
    Publisher<String> chatBlendingStream(List<Message> messages, Map<String, Object> parameters);
}
