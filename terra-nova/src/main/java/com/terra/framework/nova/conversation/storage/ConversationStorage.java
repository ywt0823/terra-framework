package com.terra.framework.nova.conversation.storage;

import com.terra.framework.nova.conversation.model.Conversation;
import com.terra.framework.nova.conversation.model.ConversationStatus;

import java.util.List;
import java.util.Optional;

public interface ConversationStorage {
    /**
     * 保存会话
     *
     * @param conversation 会话对象
     */
    void save(Conversation conversation);

    /**
     * 获取会话
     *
     * @param id 会话ID
     * @return 会话对象
     */
    Optional<Conversation> findById(String id);

    /**
     * 获取用户的所有会话
     *
     * @param userId 用户ID
     * @return 会话列表
     */
    List<Conversation> findByUserId(String userId);

    /**
     * 删除会话
     *
     * @param id 会话ID
     */
    void delete(String id);

    /**
     * 更新会话状态
     *
     * @param id 会话ID
     * @param status 新状态
     */
    void updateStatus(String id, ConversationStatus status);
} 