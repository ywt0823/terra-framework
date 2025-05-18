package com.terra.framework.nova.conversation.service.impl;

import com.terra.framework.nova.conversation.model.ConversationMessage;
import com.terra.framework.nova.conversation.service.ConversationService;
import com.terra.framework.nova.llm.blend.ModelBlender;
import com.terra.framework.nova.llm.model.AIModel;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.MessageRole;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.llm.service.BlenderService;
import com.terra.framework.nova.llm.service.EnhancedAIService;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow;

@Slf4j
public class ConversationAwareAIService implements EnhancedAIService {
    private final EnhancedAIService delegate;
    private final ConversationService conversationService;

    public ConversationAwareAIService(EnhancedAIService delegate, ConversationService conversationService) {
        this.delegate = delegate;
        this.conversationService = conversationService;
    }

    private void recordMessage(String conversationId, String content, MessageRole role) {
        if (conversationId != null) {
            ConversationMessage message = ConversationMessage.builder()
                .id(UUID.randomUUID().toString())
                .conversationId(conversationId)
                .role(role)
                .content(content)
                .createTime(LocalDateTime.now())
                .build();

            conversationService.addMessage(conversationId, message);
            log.debug("Recorded message in conversation: {}", conversationId);
        }
    }

    @Override
    public String chat(List<Message> messages, Map<String, Object> parameters) {
        String conversationId = extractConversationId(parameters);
        String response = delegate.chat(messages, parameters);
        recordMessage(conversationId, response, MessageRole.ASSISTANT);
        return response;
    }

    @Override
    public String generateText(String prompt) {
        return delegate.generateText(prompt);
    }

    @Override
    public String generateText(String prompt, Map<String, Object> parameters) {
        return delegate.generateText(prompt, parameters);
    }

    @Override
    public CompletableFuture<String> generateTextAsync(String prompt) {
        return delegate.generateTextAsync(prompt);
    }

    @Override
    public CompletableFuture<String> generateTextAsync(String prompt, Map<String, Object> parameters) {
        return delegate.generateTextAsync(prompt, parameters);
    }

    @Override
    public Flow.Publisher<String> generateTextStream(String prompt) {
        return delegate.generateTextStream(prompt);
    }

    @Override
    public Flow.Publisher<String> generateTextStream(String prompt, Map<String, Object> parameters) {
        return delegate.generateTextStream(prompt, parameters);
    }

    @Override
    public String chat(List<Message> messages) {
        return delegate.chat(messages);
    }

    @Override
    public CompletableFuture<String> chatAsync(List<Message> messages) {
        return delegate.chatAsync(messages);
    }

    @Override
    public CompletableFuture<String> chatAsync(List<Message> messages, Map<String, Object> parameters) {
        String conversationId = extractConversationId(parameters);
        return delegate.chatAsync(messages, parameters)
            .thenApply(response -> {
                recordMessage(conversationId, response, MessageRole.ASSISTANT);
                return response;
            });
    }

    @Override
    public Flow.Publisher<String> chatStream(List<Message> messages) {
        return delegate.chatStream(messages);
    }

    @Override
    public Flow.Publisher<String> chatStream(List<Message> messages, Map<String, Object> parameters) {
        return delegate.chatStream(messages, parameters);
    }

    @Override
    public ModelResponse generateResponse(String prompt, Map<String, Object> parameters) {
        return delegate.generateResponse(prompt, parameters);
    }

    @Override
    public ModelResponse chatResponse(List<Message> messages, Map<String, Object> parameters) {
        String conversationId = extractConversationId(parameters);
        ModelResponse response = delegate.chatResponse(messages, parameters);
        recordMessage(conversationId, response.getContent(), MessageRole.ASSISTANT);
        return response;
    }

    @Override
    public AIModel getModel(String modelId) {
        return delegate.getModel(modelId);
    }

    @Override
    public AIModel getModel(String vendor, String modelId) {
        return delegate.getModel(vendor, modelId);
    }

    @Override
    public ModelBlender getModelBlender() {
        return delegate.getModelBlender();
    }

    @Override
    public BlenderService getBlenderService() {
        return delegate.getBlenderService();
    }

    @Override
    public List<String> getBlenderModelIds() {
        return delegate.getBlenderModelIds();
    }

    @Override
    public boolean addModelToBlender(String modelId) {
        return delegate.addModelToBlender(modelId);
    }

    @Override
    public boolean addModelToBlender(String modelId, int weight) {
        return delegate.addModelToBlender(modelId, weight);
    }

    @Override
    public void addModelsToBlender(List<String> modelIds) {
        delegate.addModelsToBlender(modelIds);
    }

    @Override
    public void removeModelFromBlender(String modelId) {
        delegate.removeModelFromBlender(modelId);
    }

    @Override
    public String generateTextWithBlending(String prompt) {
        return delegate.generateTextWithBlending(prompt);
    }

    @Override
    public String generateTextWithBlending(String prompt, Map<String, Object> parameters) {
        return delegate.generateTextWithBlending(prompt, parameters);
    }

    @Override
    public CompletableFuture<String> generateTextWithBlendingAsync(String prompt) {
        return delegate.generateTextWithBlendingAsync(prompt);
    }

    @Override
    public CompletableFuture<String> generateTextWithBlendingAsync(String prompt, Map<String, Object> parameters) {
        return delegate.generateTextWithBlendingAsync(prompt, parameters);
    }

    @Override
    public String chatWithBlending(List<Message> messages) {
        return delegate.chatWithBlending(messages);
    }

    @Override
    public String chatWithBlending(List<Message> messages, Map<String, Object> parameters) {
        String conversationId = extractConversationId(parameters);
        String response = delegate.chatWithBlending(messages, parameters);
        recordMessage(conversationId, response, MessageRole.ASSISTANT);
        return response;
    }

    @Override
    public CompletableFuture<String> chatWithBlendingAsync(List<Message> messages) {
        return delegate.chatWithBlendingAsync(messages);
    }

    @Override
    public CompletableFuture<String> chatWithBlendingAsync(List<Message> messages, Map<String, Object> parameters) {
        String conversationId = extractConversationId(parameters);
        return delegate.chatWithBlendingAsync(messages, parameters)
            .thenApply(response -> {
                recordMessage(conversationId, response, MessageRole.ASSISTANT);
                return response;
            });
    }

    @Override
    public Flow.Publisher<String> chatBlendingStream(List<Message> messages) {
        return delegate.chatBlendingStream(messages);
    }

    @Override
    public Flow.Publisher<String> chatBlendingStream(List<Message> messages, Map<String, Object> parameters) {
        return delegate.chatBlendingStream(messages, parameters);
    }

    @Override
    public Flow.Publisher<String> generateBlendingTextStream(String prompt) {
        return delegate.generateBlendingTextStream(prompt);
    }

    @Override
    public Flow.Publisher<String> generateBlendingTextStream(String prompt, Map<String, Object> parameters) {
        return delegate.generateBlendingTextStream(prompt, parameters);
    }

    @Override
    public ModelResponse generateResponseWithBlending(String prompt, Map<String, Object> parameters) {
        return delegate.generateResponseWithBlending(prompt, parameters);
    }

    @Override
    public ModelResponse chatResponseWithBlending(List<Message> messages, Map<String, Object> parameters) {
        String conversationId = extractConversationId(parameters);
        ModelResponse response = delegate.chatResponseWithBlending(messages, parameters);
        recordMessage(conversationId, response.getContent(), MessageRole.ASSISTANT);
        return response;
    }

    private String extractConversationId(Map<String, Object> parameters) {
        return parameters != null ? (String) parameters.get("conversationId") : null;
    }
}
