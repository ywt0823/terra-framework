package com.terra.framework.nova.conversation.service.impl;

import com.terra.framework.nova.conversation.model.Conversation;
import com.terra.framework.nova.conversation.model.ConversationMessage;
import com.terra.framework.nova.conversation.model.ConversationStatus;
import com.terra.framework.nova.conversation.service.ConversationService;
import com.terra.framework.nova.conversation.storage.ConversationStorage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
public class DefaultConversationService implements ConversationService {
    private final ConversationStorage storage;

    public DefaultConversationService(ConversationStorage storage) {
        this.storage = storage;
    }

    @Override
    public Conversation createConversation(String userId, String title) {
        Conversation conversation = Conversation.builder()
            .id(UUID.randomUUID().toString())
            .userId(userId)
            .title(title)
            .status(ConversationStatus.ACTIVE)
            .messages(new ArrayList<>())
            .metadata(new HashMap<>())
            .createTime(LocalDateTime.now())
            .updateTime(LocalDateTime.now())
            .build();

        storage.save(conversation);
        log.info("Created new conversation: {}", conversation.getId());
        return conversation;
    }

    @Override
    public void addMessage(String conversationId, ConversationMessage message) {
        storage.findById(conversationId).ifPresent(conversation -> {
            conversation.getMessages().add(message);
            conversation.setUpdateTime(LocalDateTime.now());
            storage.save(conversation);
            log.debug("Added message to conversation: {}", conversationId);
        });
    }

    @Override
    public Optional<Conversation> getConversation(String conversationId) {
        return storage.findById(conversationId);
    }

    @Override
    public List<Conversation> getUserConversations(String userId) {
        return storage.findByUserId(userId);
    }

    @Override
    public void archiveConversation(String conversationId) {
        storage.updateStatus(conversationId, ConversationStatus.ARCHIVED);
        log.info("Archived conversation: {}", conversationId);
    }

    @Override
    public void deleteConversation(String conversationId) {
        storage.delete(conversationId);
        log.info("Deleted conversation: {}", conversationId);
    }
}
