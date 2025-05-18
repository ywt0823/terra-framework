package com.terra.framework.nova.conversation.storage.impl;

import com.terra.framework.nova.conversation.model.Conversation;
import com.terra.framework.nova.conversation.model.ConversationStatus;
import com.terra.framework.nova.conversation.storage.ConversationStorage;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;

@Slf4j
public class LocalConversationStorage implements ConversationStorage {
    private final ConcurrentMap<String, Conversation> conversations = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> userConversations = new ConcurrentHashMap<>();

    @Override
    public void save(Conversation conversation) {
        conversations.put(conversation.getId(), conversation);
        userConversations.compute(conversation.getUserId(), (userId, convIds) -> {
            if (convIds == null) {
                convIds = ConcurrentHashMap.newKeySet();
            }
            convIds.add(conversation.getId());
            return convIds;
        });
        log.debug("Saved conversation: {}", conversation.getId());
    }

    @Override
    public Optional<Conversation> findById(String id) {
        return Optional.ofNullable(conversations.get(id));
    }

    @Override
    public List<Conversation> findByUserId(String userId) {
        return userConversations.getOrDefault(userId, Collections.emptySet())
            .stream()
            .map(conversations::get)
            .filter(Objects::nonNull)
            .collect(Collectors.toList());
    }

    @Override
    public void delete(String id) {
        Conversation removed = conversations.remove(id);
        if (removed != null) {
            userConversations.computeIfPresent(removed.getUserId(), (userId, convIds) -> {
                convIds.remove(id);
                return convIds.isEmpty() ? null : convIds;
            });
            log.debug("Deleted conversation: {}", id);
        }
    }

    @Override
    public void updateStatus(String id, ConversationStatus status) {
        conversations.computeIfPresent(id, (convId, conv) -> {
            conv.setStatus(status);
            conv.setUpdateTime(LocalDateTime.now());
            return conv;
        });
        log.debug("Updated conversation status: {} -> {}", id, status);
    }
}
