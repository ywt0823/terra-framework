package com.terra.framework.nova.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.ai.chat.prompt.SystemPromptTemplate;
import org.springframework.ai.document.Document;
import org.springframework.ai.transformer.splitter.TokenTextSplitter;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class RagServiceImpl implements RagService {

    private final ChatModel chatModel;
    private final VectorStore vectorStore;

    private static final String SYSTEM_PROMPT_TEMPLATE = """
            You are a helpful assistant. You have to answer the question based on the context provided.
            If the context does not contain the answer, you can use your own knowledge.
            
            Context:
            {context}
            """;

    @Autowired
    public RagServiceImpl(ChatModel chatModel, VectorStore vectorStore) {
        this.chatModel = chatModel;
        this.vectorStore = vectorStore;
    }

    @Override
    public void load(List<Document> documents) {
        log.info("Loading {} documents into vector store.", documents.size());
        TokenTextSplitter textSplitter = new TokenTextSplitter();
        List<Document> splitDocs = textSplitter.apply(documents);
        this.vectorStore.add(splitDocs);
        log.info("Successfully loaded {} documents.", documents.size());
    }

    @Override
    public String ask(String query) {
        log.info("Searching for relevant documents for query: {}", query);
        List<Document> similarDocuments = this.vectorStore.similaritySearch(query);
        String context = similarDocuments.stream()
                .map(Document::getContent)
                .collect(Collectors.joining(System.lineSeparator()));

        log.info("Found {} relevant documents. Creating prompt.", similarDocuments.size());
        SystemPromptTemplate systemPromptTemplate = new SystemPromptTemplate(SYSTEM_PROMPT_TEMPLATE);
        Prompt prompt = new Prompt(systemPromptTemplate.createMessage(Map.of("context", context)));
        
        log.info("Calling chat model to get answer.");
        return this.chatModel.call(prompt).getResult().getOutput().getContent();
    }

    @Override
    public void deleteAll() {
        log.warn("Deleting all documents from vector store. This operation cannot be undone.");
        // Note: Not all VectorStore implementations support delete.
        // This is a placeholder for a more robust implementation.
        // For SimpleVectorStore, we would need to re-initialize it.
        // For RedisVectorStore, we could delete the index.
        log.warn("Delete operation is not implemented for the current vector store.");
    }
} 