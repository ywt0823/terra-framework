package com.terra.framework.nova.blend;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import com.terra.framework.nova.model.AIModel;
import com.terra.framework.nova.model.Message;
import com.terra.framework.nova.model.ModelResponse;

/**
 * 模型混合调用接口
 *
 * @author terra-nova
 */
public interface ModelBlender {

    /**
     * 使用多个模型生成文本（同步）
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @return 混合结果
     */
    ModelResponse generateWithMultipleModels(String prompt, Map<String, Object> parameters);

    /**
     * 使用多个模型生成文本（异步）
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @return 异步混合结果
     */
    CompletableFuture<ModelResponse> generateWithMultipleModelsAsync(String prompt, Map<String, Object> parameters);

    /**
     * 使用多个模型进行聊天（同步）
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @return 混合结果
     */
    ModelResponse chatWithMultipleModels(List<Message> messages, Map<String, Object> parameters);

    /**
     * 使用多个模型进行聊天（异步）
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @return 异步混合结果
     */
    CompletableFuture<ModelResponse> chatWithMultipleModelsAsync(List<Message> messages, Map<String, Object> parameters);

    /**
     * 添加模型到混合器
     *
     * @param model 模型
     * @param weight 权重（0-100）
     * @return 当前混合器实例（链式调用）
     */
    ModelBlender addModel(AIModel model, int weight);

    /**
     * 添加模型到混合器（使用默认权重）
     *
     * @param model 模型
     * @return 当前混合器实例（链式调用）
     */
    ModelBlender addModel(AIModel model);

    /**
     * 移除模型
     *
     * @param modelId 模型ID
     * @return 当前混合器实例（链式调用）
     */
    ModelBlender removeModel(String modelId);

    /**
     * 获取所有模型
     *
     * @return 模型列表
     */
    List<AIModel> getModels();

    /**
     * 设置结果合并策略
     *
     * @param mergeStrategy 合并策略
     * @return 当前混合器实例（链式调用）
     */
    ModelBlender setMergeStrategy(MergeStrategy mergeStrategy);

    /**
     * 获取当前的合并策略
     *
     * @return 合并策略
     */
    MergeStrategy getMergeStrategy();
}
