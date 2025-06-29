package com.terra.framework.nova.template;

import com.terra.framework.nova.memory.ConversationMemory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.messages.Message;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.Prompt;

import java.util.ArrayList;
import java.util.List;

/**
 * Template class that simplifies stateful conversational interactions with an AI model.
 * This class is designed to be thread-safe.
 *
 * @see ConversationOperations
 */
@Slf4j
@RequiredArgsConstructor
public class ConversationTemplate implements ConversationOperations {

    private final ChatModel chatModel;
    private final ConversationMemory conversationMemory;

    @Override
    public String chat(String sessionId, String message) {
        log.info("Starting chat for session [{}].", sessionId);

        List<Message> history = conversationMemory.get(sessionId).orElse(new ArrayList<>());
        log.debug("Retrieved {} messages from history for session [{}].", history.size(), sessionId);

        history.add(new UserMessage(message));

        Prompt prompt = new Prompt(history);
        ChatResponse response = chatModel.call(prompt);

        Message assistantMessage = response.getResult().getOutput();

        history.add(assistantMessage);

        conversationMemory.add(sessionId, history);
        log.info("Chat finished for session [{}]. Saved {} messages to history.", sessionId, history.size());

        return assistantMessage.getContent();
    }

    @Override
    public void clearHistory(String sessionId) {
        log.info("Clearing conversation history for session [{}].", sessionId);
        conversationMemory.clear(sessionId);
    }
}
