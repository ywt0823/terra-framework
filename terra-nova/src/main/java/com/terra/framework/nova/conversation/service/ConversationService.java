package com.terra.framework.nova.conversation.service;

import com.terra.framework.nova.conversation.model.Conversation;
import com.terra.framework.nova.conversation.model.ConversationMessage;

import java.util.List;
import java.util.Optional;

public interface ConversationService {
    /**
     * 创建新会话
     *
     * @param userId 用户ID
     * @param title 会话标题
     * @return 创建的会话
     */
    Conversation createConversation(String userId, String title);

    /**
     * 添加消息到会话
     *
     * @param conversationId 会话ID
     * @param message 消息内容
     */
    void addMessage(String conversationId, ConversationMessage message);

    /**
     * 获取会话
     *
     * @param conversationId 会话ID
     * @return 会话对象
     */
    Optional<Conversation> getConversation(String conversationId);

    /**
     * 获取用户的所有会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Conversation> getUserConversations(String userId);

    /**
     * 归档会话
     *
     * @param conversationId 会话ID
     */
    void archiveConversation(String conversationId);

    /**
     * 删除会话
     *
     * @param conversationId 会话ID
     */
    void deleteConversation(String conversationId);
} 