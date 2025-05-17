package com.terra.framework.nova.core.service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.SubmissionPublisher;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terra.framework.nova.core.blend.ModelBlender;
import com.terra.framework.nova.core.model.AIModel;
import com.terra.framework.nova.core.model.AIModelManager;
import com.terra.framework.nova.core.model.Message;
import com.terra.framework.nova.core.model.ModelResponse;

/**
 * 模型混合器服务，提供混合调用多个模型的功能
 *
 * @author terra-nova
 */
public class BlenderService {

    private static final Logger log = LoggerFactory.getLogger(BlenderService.class);

    /**
     * 模型混合器
     */
    private final ModelBlender blender;

    /**
     * 模型管理器
     */
    private final AIModelManager modelManager;

    /**
     * 构造函数
     *
     * @param blender 模型混合器
     * @param modelManager 模型管理器
     */
    public BlenderService(ModelBlender blender, AIModelManager modelManager) {
        this.blender = blender;
        this.modelManager = modelManager;
    }

    /**
     * 添加所有可用模型到混合器
     */
    public void addAllAvailableModels() {
        log.info("自动添加所有可用模型到混合器");
        List<AIModel> models = modelManager.getAllModels();
        if (models.isEmpty()) {
            log.warn("没有找到可用的模型");
            return;
        }

        models.forEach(model -> {
            try {
                blender.addModel(model);
                log.info("已添加模型: {}", model.getModelInfo().getModelId());
            } catch (Exception e) {
                log.error("添加模型失败: {}", model.getModelInfo().getModelId(), e);
            }
        });
    }

    /**
     * 添加指定的模型到混合器
     *
     * @param modelIds 模型ID列表
     */
    public void addModels(List<String> modelIds) {
        log.info("添加指定模型到混合器: {}", modelIds);
        if (modelIds == null || modelIds.isEmpty()) {
            log.warn("模型ID列表为空");
            return;
        }

        modelIds.forEach(this::addModel);
    }

    /**
     * 添加单个模型到混合器
     *
     * @param modelId 模型ID
     * @return 是否添加成功
     */
    public boolean addModel(String modelId) {
        try {
            AIModel model = modelManager.getModel(modelId);
            blender.addModel(model);
            log.info("已添加模型: {}", modelId);
            return true;
        } catch (Exception e) {
            log.error("添加模型失败: {}", modelId, e);
            return false;
        }
    }

    /**
     * 添加带权重的模型
     *
     * @param modelId 模型ID
     * @param weight 权重值（0-100）
     * @return 是否添加成功
     */
    public boolean addModel(String modelId, int weight) {
        try {
            AIModel model = modelManager.getModel(modelId);
            blender.addModel(model, weight);
            log.info("已添加模型: {}, 权重: {}", modelId, weight);
            return true;
        } catch (Exception e) {
            log.error("添加模型失败: {}", modelId, e);
            return false;
        }
    }

    /**
     * 混合生成文本
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @return 混合结果
     */
    public String generateText(String prompt, Map<String, Object> parameters) {
        ModelResponse response = blender.generateWithMultipleModels(prompt, parameters);
        return response.getContent();
    }

    /**
     * 异步混合生成文本
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @return 异步混合结果
     */
    public CompletableFuture<String> generateTextAsync(String prompt, Map<String, Object> parameters) {
        return blender.generateWithMultipleModelsAsync(prompt, parameters)
                .thenApply(ModelResponse::getContent);
    }

    /**
     * 混合聊天
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @return 混合结果
     */
    public String chat(List<Message> messages, Map<String, Object> parameters) {
        ModelResponse response = blender.chatWithMultipleModels(messages, parameters);
        return response.getContent();
    }

    /**
     * 异步混合聊天
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @return 异步混合结果
     */
    public CompletableFuture<String> chatAsync(List<Message> messages, Map<String, Object> parameters) {
        return blender.chatWithMultipleModelsAsync(messages, parameters)
                .thenApply(ModelResponse::getContent);
    }

    /**
     * 获取所有已添加的模型ID
     *
     * @return 模型ID列表
     */
    public List<String> getAddedModelIds() {
        return blender.getModels().stream()
                .map(model -> model.getModelInfo().getModelId())
                .collect(Collectors.toList());
    }

    /**
     * 移除模型
     *
     * @param modelId 模型ID
     */
    public void removeModel(String modelId) {
        blender.removeModel(modelId);
        log.info("已移除模型: {}", modelId);
    }

    /**
     * 清除所有模型
     */
    public void clearModels() {
        List<String> modelIds = getAddedModelIds();
        modelIds.forEach(this::removeModel);
        log.info("已清除所有模型");
    }

    /**
     * 流式混合生成文本
     *
     * @param prompt 提示词
     * @param parameters 参数
     * @return 响应流
     */
    public Publisher<String> generateTextStream(String prompt, Map<String, Object> parameters) {
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        final AtomicBoolean completed = new AtomicBoolean(false);

        // 异步执行混合生成
        blender.generateWithMultipleModelsAsync(prompt, parameters)
                .thenAccept(response -> {
                    if (completed.compareAndSet(false, true)) {
                        // 将结果发送到流中
                        publisher.submit(response.getContent());
                        publisher.close();
                    }
                })
                .exceptionally(ex -> {
                    if (completed.compareAndSet(false, true)) {
                        publisher.closeExceptionally(ex);
                    }
                    return null;
                });

        return publisher;
    }

    /**
     * 流式混合聊天
     *
     * @param messages 消息列表
     * @param parameters 参数
     * @return 响应流
     */
    public Publisher<String> chatStream(List<Message> messages, Map<String, Object> parameters) {
        SubmissionPublisher<String> publisher = new SubmissionPublisher<>();
        final AtomicBoolean completed = new AtomicBoolean(false);

        // 异步执行混合聊天
        blender.chatWithMultipleModelsAsync(messages, parameters)
                .thenAccept(response -> {
                    if (completed.compareAndSet(false, true)) {
                        // 将结果发送到流中
                        publisher.submit(response.getContent());
                        publisher.close();
                    }
                })
                .exceptionally(ex -> {
                    if (completed.compareAndSet(false, true)) {
                        publisher.closeExceptionally(ex);
                    }
                    return null;
                });

        return publisher;
    }

    /**
     * 获取混合器
     *
     * @return 模型混合器
     */
    public ModelBlender getBlender() {
        return blender;
    }
}
