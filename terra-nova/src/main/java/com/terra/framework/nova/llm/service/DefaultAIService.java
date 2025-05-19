package com.terra.framework.nova.llm.service;

import com.terra.framework.nova.llm.model.AIModel;
import com.terra.framework.nova.llm.model.AIModelManager;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.ModelResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

/**
 * AI服务默认实现
 *
 * @author terra-nova
 */
@Slf4j
public abstract class DefaultAIService implements AIService {

    /**
     * 模型管理器
     */
    private final AIModelManager modelManager;

    /**
     * 默认模型ID
     */
    private final String defaultModelId;

    /**
     * 构造函数
     *
     * @param modelManager   模型管理器
     * @param defaultModelId 默认模型ID
     */
    public DefaultAIService(AIModelManager modelManager, String defaultModelId) {
        this.modelManager = modelManager;
        this.defaultModelId = defaultModelId;
    }

    @Override
    public String generateText(String prompt) {
        return generateText(prompt, new HashMap<>());
    }

    @Override
    public String generateText(String prompt, String modelId) {
        return generateText(prompt, modelId, new HashMap<>());
    }

    @Override
    public String generateText(String prompt, Map<String, Object> parameters) {
        return generateResponse(prompt, parameters).getContent();
    }

    @Override
    public String generateText(String prompt, String modelId, Map<String, Object> parameters) {
        return generateResponse(prompt, modelId, parameters).getContent();
    }

    @Override
    public CompletableFuture<String> generateTextAsync(String prompt) {
        return generateTextAsync(prompt, new HashMap<>());
    }

    @Override
    public CompletableFuture<String> generateTextAsync(String prompt, Map<String, Object> parameters) {
        AIModel model = getDefaultModel();
        return model.generateAsync(prompt, parameters)
            .thenApply(ModelResponse::getContent);
    }

    @Override
    public Publisher<String> generateTextStream(String prompt) {
        return generateTextStream(prompt, new HashMap<>());
    }

    @Override
    public Publisher<String> generateTextStream(String prompt, Map<String, Object> parameters) {
        AIModel model = getDefaultModel();
        return model.generateStream(prompt, parameters);
    }

    @Override
    public String chat(List<Message> messages) {
        return chat(messages, new HashMap<>());
    }

    @Override
    public String chat(List<Message> messages, Map<String, Object> parameters) {
        return chatResponse(messages, parameters).getContent();
    }

    @Override
    public String chat(List<Message> messages, String modelId, Map<String, Object> parameters) {
        return chatResponse(messages, modelId, parameters).getContent();
    }

    @Override
    public CompletableFuture<String> chatAsync(List<Message> messages) {
        return chatAsync(messages, new HashMap<>());
    }

    @Override
    public CompletableFuture<String> chatAsync(List<Message> messages, Map<String, Object> parameters) {
        AIModel model = getDefaultModel();
        return model.chatAsync(messages, parameters)
            .thenApply(ModelResponse::getContent);
    }

    @Override
    public Publisher<String> chatStream(List<Message> messages) {
        return chatStream(messages, new HashMap<>());
    }

    @Override
    public Publisher<String> chatStream(List<Message> messages, Map<String, Object> parameters) {
        AIModel model = getDefaultModel();
        return model.chatStream(messages, parameters);
    }

    @Override
    public ModelResponse generateResponse(String prompt, Map<String, Object> parameters) {
        AIModel model = getDefaultModel();
        return model.generate(prompt, parameters);
    }

    @Override
    public ModelResponse generateResponse(String prompt, String modelId, Map<String, Object> parameters) {
        AIModel model = getModel(modelId);
        return model.generate(prompt, parameters);
    }


    @Override
    public ModelResponse chatResponse(List<Message> messages, Map<String, Object> parameters) {
        AIModel model = getDefaultModel();
        return model.chat(messages, parameters);
    }

    @Override
    public ModelResponse chatResponse(List<Message> messages, String modelId, Map<String, Object> parameters) {
        AIModel model = getModel(modelId);
        return model.chat(messages, parameters);
    }

    @Override
    public AIModel getModel(String modelId) {
        return modelManager.getModel(modelId);
    }

    @Override
    public AIModel getModel(String vendor, String modelId) {
        // 构建完整的模型ID，格式为：vendor:modelId
        String fullModelId = vendor.toLowerCase() + ":" + modelId;
        return getModel(fullModelId);
    }

    /**
     * 获取默认模型
     *
     * @return 默认模型实例
     */
    private AIModel getDefaultModel() {
        return modelManager.getModel(defaultModelId);
    }
}
