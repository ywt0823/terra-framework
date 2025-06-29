package com.terra.framework.nova.service;

import org.springframework.ai.document.Document;
import java.util.List;

/**
 * Interface defining the operations for Retrieval-Augmented Generation (RAG).
 * This follows the Spring Framework's `...Operations` pattern.
 */
public interface RagOperations {

    /**
     * Adds a list of documents to the underlying vector store.
     * The documents are automatically split into smaller chunks before being added.
     *
     * @param documents The list of documents to add.
     */
    void add(List<Document> documents);

    /**
     * Asks a question to the AI model using the RAG pattern.
     * The method will first find relevant documents from the vector store
     * and use them as context to answer the question.
     *
     * @param query The user's question.
     * @return The AI's answer.
     */
    String ask(String query);
} 