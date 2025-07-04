package com.terra.framework.nova.template;

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


}
