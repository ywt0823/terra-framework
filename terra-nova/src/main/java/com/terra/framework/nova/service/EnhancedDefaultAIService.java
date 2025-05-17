package com.terra.framework.nova.service;

import com.terra.framework.nova.blend.ModelBlender;
import com.terra.framework.nova.model.AIModelManager;
import com.terra.framework.nova.model.Message;
import com.terra.framework.nova.model.ModelResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

import lombok.extern.slf4j.Slf4j;

/**
 * 增强型AI服务默认实现
 *
 * @author terra-nova
 */
@Slf4j
public class EnhancedDefaultAIService extends DefaultAIService implements EnhancedAIService {

    /**
     * 混合器服务
     */
    private final BlenderService blenderService;

    /**
     * 构造函数
     *
     * @param modelManager 模型管理器
     * @param defaultModelId 默认模型ID
     * @param blenderService 混合器服务
     */
    public EnhancedDefaultAIService(AIModelManager modelManager, String defaultModelId, BlenderService blenderService) {
        super(modelManager, defaultModelId);
        this.blenderService = blenderService;
    }

    @Override
    public BlenderService getBlenderService() {
        return blenderService;
    }

    @Override
    public ModelBlender getModelBlender() {
        return blenderService.getBlender();
    }

    @Override
    public String generateTextWithBlending(String prompt) {
        return generateTextWithBlending(prompt, new HashMap<>());
    }

    @Override
    public String generateTextWithBlending(String prompt, Map<String, Object> parameters) {
        return blenderService.generateText(prompt, parameters);
    }

    @Override
    public CompletableFuture<String> generateTextWithBlendingAsync(String prompt) {
        return generateTextWithBlendingAsync(prompt, new HashMap<>());
    }

    @Override
    public CompletableFuture<String> generateTextWithBlendingAsync(String prompt, Map<String, Object> parameters) {
        return blenderService.generateTextAsync(prompt, parameters);
    }

    @Override
    public String chatWithBlending(List<Message> messages) {
        return chatWithBlending(messages, new HashMap<>());
    }

    @Override
    public String chatWithBlending(List<Message> messages, Map<String, Object> parameters) {
        return blenderService.chat(messages, parameters);
    }

    @Override
    public CompletableFuture<String> chatWithBlendingAsync(List<Message> messages) {
        return chatWithBlendingAsync(messages, new HashMap<>());
    }

    @Override
    public CompletableFuture<String> chatWithBlendingAsync(List<Message> messages, Map<String, Object> parameters) {
        return blenderService.chatAsync(messages, parameters);
    }

    @Override
    public ModelResponse generateResponseWithBlending(String prompt, Map<String, Object> parameters) {
        return blenderService.getBlender().generateWithMultipleModels(prompt, parameters);
    }

    @Override
    public ModelResponse chatResponseWithBlending(List<Message> messages, Map<String, Object> parameters) {
        return blenderService.getBlender().chatWithMultipleModels(messages, parameters);
    }

    @Override
    public boolean addModelToBlender(String modelId) {
        return blenderService.addModel(modelId);
    }

    @Override
    public boolean addModelToBlender(String modelId, int weight) {
        return blenderService.addModel(modelId, weight);
    }

    @Override
    public void addModelsToBlender(List<String> modelIds) {
        blenderService.addModels(modelIds);
    }

    @Override
    public void removeModelFromBlender(String modelId) {
        blenderService.removeModel(modelId);
    }

    @Override
    public List<String> getBlenderModelIds() {
        return blenderService.getAddedModelIds();
    }

    @Override
    public Publisher<String> generateBlendingTextStream(String prompt) {
        return generateBlendingTextStream(prompt, new HashMap<>());
    }

    @Override
    public Publisher<String> generateBlendingTextStream(String prompt, Map<String, Object> parameters) {
        return blenderService.generateTextStream(prompt, parameters);
    }

    @Override
    public Publisher<String> chatBlendingStream(List<Message> messages) {
        return chatBlendingStream(messages, new HashMap<>());
    }

    @Override
    public Publisher<String> chatBlendingStream(List<Message> messages, Map<String, Object> parameters) {
        return blenderService.chatStream(messages, parameters);
    }
} 