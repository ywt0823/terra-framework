package com.terra.framework.nova.memory;

import com.terra.framework.nova.properties.TerraAiProperties;
import org.springframework.ai.chat.messages.Message;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryConversationMemory implements ConversationMemory {

    private final String id = UUID.randomUUID().toString();
    private final Map<String, LinkedList<Message>> messageHistory = new ConcurrentHashMap<>();
    private final int maxHistory;

    public InMemoryConversationMemory(TerraAiProperties.MemoryProperties memoryProperties) {
        this.maxHistory = memoryProperties.getMaxHistory();
    }

    @Override
    public String getId() {
        return this.id;
    }

    @Override
    public Optional<List<Message>> get(String sessionId) {
        LinkedList<Message> history = this.messageHistory.get(sessionId);
        if (history == null) {
            return Optional.empty();
        }
        // Return an immutable copy for thread safety
        synchronized (history) {
            return Optional.of(List.copyOf(history));
        }
    }

    @Override
    public void add(String sessionId, List<Message> messages) {
        LinkedList<Message> history = this.messageHistory.computeIfAbsent(sessionId, k -> new LinkedList<>());

        synchronized (history) {
            history.clear();
            history.addAll(messages);
            // Trim history to max size
            while (history.size() > this.maxHistory) {
                history.removeFirst();
            }
        }
    }

    @Override
    public void clear(String sessionId) {
        this.messageHistory.remove(sessionId);
    }
} 