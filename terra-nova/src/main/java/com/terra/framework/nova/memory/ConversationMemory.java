package com.terra.framework.nova.memory;

import org.springframework.ai.chat.messages.Message;

import java.util.List;
import java.util.Optional;

/**
 * An interface for managing conversation history for different sessions.
 */
public interface ConversationMemory {

    /**
     * Gets the unique ID for this memory store instance.
     * @return the memory ID.
     */
    String getId();

    /**
     * Retrieves the entire message history for a given session.
     * @param sessionId The unique identifier for the conversation session.
     * @return A list of messages, or an empty Optional if no history is found.
     */
    Optional<List<Message>> get(String sessionId);

    /**
     * Adds/updates the list of messages for a given session's history.
     * This will typically replace the existing history.
     * @param sessionId The unique identifier for the conversation session.
     * @param messages The list of messages to store.
     */
    void add(String sessionId, List<Message> messages);

    /**
     * Clears the history for a given session.
     * @param sessionId The unique identifier for the conversation session.
     */
    void clear(String sessionId);
} 