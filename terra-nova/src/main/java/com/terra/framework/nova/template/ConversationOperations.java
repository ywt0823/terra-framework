package com.terra.framework.nova.template;

/**
 * Interface defining the operations for handling stateful conversations with an AI model.
 * This follows the Spring Framework's `...Operations` pattern (e.g., JdbcOperations).
 */
public interface ConversationOperations {

    /**
     * Sends a message within a specific conversation session and gets a response.
     * The implementation will manage the conversation history automatically.
     *
     * @param sessionId A unique identifier for the conversation session.
     * @param message   The user's message.
     * @return The AI's response message content.
     */
    String chat(String sessionId, String message);

    /**
     * Clears the conversation history for a given session.
     *
     * @param sessionId The unique identifier for the conversation session to clear.
     */
    void clearHistory(String sessionId);
}
