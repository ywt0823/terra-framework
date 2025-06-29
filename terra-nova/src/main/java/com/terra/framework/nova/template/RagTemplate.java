package com.terra.framework.nova.template;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

    private final EmbeddingModel embeddingModel;
    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    @Value("${spring.ai.prompt.system.default:You are a helpful assistant. You must answer the question based on the context provided. Context: {context} --- Question: {question}}")
    private String systemPrompt;

    @Override
    public void add(List<Document> documents) {
        log.info("Adding {} documents to the vector store.", documents.size());
        TokenTextSplitter tokenTextSplitter = new TokenTextSplitter();
        List<Document> splitDocuments = tokenTextSplitter.apply(documents);
        log.info("Adding {} documents to the vector store after splitting.", splitDocuments.size());
        this.vectorStore.add(splitDocuments);
    }

    @Override
    public String ask(String query) {
        log.info("Searching for relevant documents for query: {}", query);
        SearchRequest searchRequest = SearchRequest.query(query).withTopK(TOP_K);
        List<Document> similarDocuments = this.vectorStore.similaritySearch(searchRequest);
        String context = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining("\n"));

        log.info("Found {} relevant documents. Creating prompt.", similarDocuments.size());
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(systemPrompt);
        Prompt prompt = new Prompt(systemPromptTemplate.createMessage(Map.of("context", context, "question", query)));

        return this.chatModel.call(prompt).getResult().getOutput().getContent();
    }
}
