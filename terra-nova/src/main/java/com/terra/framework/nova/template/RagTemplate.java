package com.terra.framework.nova.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;

import java.util.List;

/**
 * Template class that simplifies Retrieval-Augmented Generation (RAG) interactions.
 * This class is designed to be thread-safe.
 *
 * @see RagOperations
 */
@Slf4j
@RequiredArgsConstructor
public class RagTemplate implements RagOperations {

    private static final int TOP_K = 1;

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    @Override
    public void add(List<Document> documents) {
        log.info("Adding {} documents to the vector store.", documents.size());
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = tokenTextSplitter.apply(documents);
        log.info("Adding {} documents to the vector store after splitting.", splitDocuments.size());
        this.vectorStore.add(splitDocuments);
    }

}
