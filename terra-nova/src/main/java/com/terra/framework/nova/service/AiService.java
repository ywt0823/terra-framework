package com.terra.framework.nova.service;

import lombok.RequiredArgsConstructor;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.VectorStore;

/**
 * Unified service facade for AI capabilities.
 *
 * @author AI
 */
@RequiredArgsConstructor
public class AiService {

    private final ChatClient chatClient;
    private final VectorStore vectorStore;

    /**
     * A simple chat method that takes a message and returns the model's response.
     *
     * @param message The user's message.
     * @return The AI model's response.
     */
    public String chat(String message) {
        return chatClient.prompt()
                .user(message)
                .call()
                .content();
    }


    /**
     * Asks a question using Retrieval-Augmented Generation (RAG).
     * The QuestionAnswerAdvisor will find similar documents in the vector store
     * and use them as context to answer the user's question.
     *
     * @param question The user's question.
     * @return The AI model's answer, based on the retrieved context.
     */
    public String chatWithRag(String question) {
        return chatClient.prompt()
                .user(question)
                .advisors(new QuestionAnswerAdvisor(vectorStore))
                .call()
                .content();
    }
}
