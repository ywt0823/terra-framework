package com.terra.framework.nova.prompt.history;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

/**
 * 提示历史记录，用于跟踪和管理对话历史
 *
 * @author terra-nova
 */
@Data
public class PromptHistory {
    
    /**
     * 会话ID
     */
    private String sessionId;
    
    /**
     * 用户ID
     */
    private String userId;
    
    /**
     * 会话开始时间
     */
    private LocalDateTime startTime;
    
    /**
     * 会话最后更新时间
     */
    private LocalDateTime lastUpdateTime;
    
    /**
     * 模型名称
     */
    private String model;
    
    /**
     * 系统提示
     */
    private String systemPrompt;
    
    /**
     * 消息记录
     */
    private List<PromptMessage> messages;
    
    /**
     * 创建一个新的会话历史记录
     *
     * @param userId 用户ID
     * @param model 模型名称
     * @return 会话历史记录
     */
    public static PromptHistory create(String userId, String model) {
        return create(userId, model, null);
    }
    
    /**
     * 创建一个新的会话历史记录
     *
     * @param userId 用户ID
     * @param model 模型名称
     * @param systemPrompt 系统提示
     * @return 会话历史记录
     */
    public static PromptHistory create(String userId, String model, String systemPrompt) {
        PromptHistory history = new PromptHistory();
        history.sessionId = UUID.randomUUID().toString();
        history.userId = userId;
        history.startTime = LocalDateTime.now();
        history.lastUpdateTime = history.startTime;
        history.model = model;
        history.systemPrompt = systemPrompt;
        history.messages = new ArrayList<>();
        return history;
    }
    
    /**
     * 添加用户消息
     *
     * @param content 消息内容
     * @return 当前实例
     */
    public PromptHistory addUserMessage(String content) {
        PromptMessage message = new PromptMessage();
        message.setRole("user");
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        
        if (messages == null) {
            messages = new ArrayList<>();
        }
        
        messages.add(message);
        lastUpdateTime = message.getTimestamp();
        return this;
    }
    
    /**
     * 添加助手消息
     *
     * @param content 消息内容
     * @return 当前实例
     */
    public PromptHistory addAssistantMessage(String content) {
        PromptMessage message = new PromptMessage();
        message.setRole("assistant");
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        
        if (messages == null) {
            messages = new ArrayList<>();
        }
        
        messages.add(message);
        lastUpdateTime = message.getTimestamp();
        return this;
    }
    
    /**
     * 添加系统消息
     *
     * @param content 消息内容
     * @return 当前实例
     */
    public PromptHistory addSystemMessage(String content) {
        PromptMessage message = new PromptMessage();
        message.setRole("system");
        message.setContent(content);
        message.setTimestamp(LocalDateTime.now());
        
        if (messages == null) {
            messages = new ArrayList<>();
        }
        
        messages.add(message);
        lastUpdateTime = message.getTimestamp();
        return this;
    }
    
    /**
     * 获取所有用户消息
     *
     * @return 用户消息列表
     */
    public List<String> getUserMessages() {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> userMessages = new ArrayList<>();
        for (PromptMessage message : messages) {
            if ("user".equals(message.getRole())) {
                userMessages.add(message.getContent());
            }
        }
        
        return userMessages;
    }
    
    /**
     * 获取所有助手消息
     *
     * @return 助手消息列表
     */
    public List<String> getAssistantMessages() {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        
        List<String> assistantMessages = new ArrayList<>();
        for (PromptMessage message : messages) {
            if ("assistant".equals(message.getRole())) {
                assistantMessages.add(message.getContent());
            }
        }
        
        return assistantMessages;
    }
    
    /**
     * 获取会话中的消息数量
     *
     * @return 消息数量
     */
    public int getMessageCount() {
        return messages == null ? 0 : messages.size();
    }
    
    /**
     * 清空消息历史
     */
    public void clearMessages() {
        if (messages != null) {
            messages.clear();
        }
    }
    
    /**
     * 提示消息实体类
     */
    @Data
    public static class PromptMessage {
        /**
         * 消息角色
         */
        private String role;
        
        /**
         * 消息内容
         */
        private String content;
        
        /**
         * 消息时间戳
         */
        private LocalDateTime timestamp;
    }
} 