package com.terra.framework.nova.service.impl;

import com.terra.framework.nova.client.TerraChatClient;
import com.terra.framework.nova.properties.TerraNovaProperties;
import com.terra.framework.nova.service.TerraRAGService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Terra RAG 服务默认实现
 * 
 * <p>基于 Spring AI 的检索增强生成服务实现，提供：
 * <ul>
 *   <li>文档存储和检索</li>
 *   <li>向量搜索</li>
 *   <li>上下文生成</li>
 *   <li>RAG 查询处理</li>
 * </ul>
 * 
 * @author terra-nova
 * @since 0.0.1
 */
public class DefaultTerraRAGService implements TerraRAGService {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTerraRAGService.class);

    private final TerraChatClient chatClient;
    private final VectorStore vectorStore;
    private final TerraNovaProperties properties;

    public DefaultTerraRAGService(TerraChatClient chatClient, 
                                  VectorStore vectorStore,
                                  TerraNovaProperties properties) {
        this.chatClient = chatClient;
        this.vectorStore = vectorStore;
        this.properties = properties;
        logger.info("DefaultTerraRAGService initialized with vector store: {}", 
                   vectorStore.getClass().getSimpleName());
    }

    @Override
    public void addDocuments(List<Document> documents) {
        try {
            if (documents == null || documents.isEmpty()) {
                logger.warn("No documents to add");
                return;
            }

            vectorStore.add(documents);
            logger.info("Added {} documents to vector store", documents.size());
        } catch (Exception e) {
            logger.error("Error adding documents to vector store", e);
            throw new RuntimeException("Failed to add documents", e);
        }
    }

    @Override
    public void addDocument(Document document) {
        addDocuments(Collections.singletonList(document));
    }

    @Override
    public void addTextDocument(String text, Map<String, Object> metadata) {
        if (!StringUtils.hasText(text)) {
            logger.warn("Empty text provided for document");
            return;
        }

        Document document = new Document(text, metadata != null ? metadata : new HashMap<>());
        addDocument(document);
    }

    @Override
    public void deleteDocuments(List<String> documentIds) {
        try {
            if (documentIds == null || documentIds.isEmpty()) {
                logger.warn("No document IDs provided for deletion");
                return;
            }

            vectorStore.delete(documentIds);
            logger.info("Deleted {} documents from vector store", documentIds.size());
        } catch (Exception e) {
            logger.error("Error deleting documents from vector store", e);
            throw new RuntimeException("Failed to delete documents", e);
        }
    }

    @Override
    public void clearDocuments() {
        try {
            // Spring AI VectorStore 可能没有直接的清空方法
            // 这里需要根据具体实现来处理
            logger.warn("Clear all documents operation may not be supported by all vector stores");
            // vectorStore.clear(); // 如果支持的话
        } catch (Exception e) {
            logger.error("Error clearing documents from vector store", e);
            throw new RuntimeException("Failed to clear documents", e);
        }
    }

    @Override
    public List<Document> searchSimilarDocuments(String query, QueryOptions options) {
        try {
            if (!StringUtils.hasText(query)) {
                logger.warn("Empty query provided for similarity search");
                return Collections.emptyList();
            }

            List<Document> results = vectorStore.similaritySearch(query, options.toSearchRequest());
            
            logger.debug("Found {} similar documents for query: {}", results.size(), query);
            return results;
        } catch (Exception e) {
            logger.error("Error searching similar documents", e);
            throw new RuntimeException("Failed to search similar documents", e);
        }
    }

    @Override
    public List<Document> searchSimilarDocuments(String query) {
        return searchSimilarDocuments(query, QueryOptions.builder());
    }

    @Override
    public String query(String query, QueryOptions options) {
        try {
            if (!StringUtils.hasText(query)) {
                throw new IllegalArgumentException("Query cannot be empty");
            }

            // 1. 检索相关文档
            List<Document> relevantDocuments = searchSimilarDocuments(query, options);
            
            if (relevantDocuments.isEmpty()) {
                logger.warn("No relevant documents found for query: {}", query);
                return "抱歉，我没有找到相关的信息来回答您的问题。";
            }

            // 2. 构建上下文
            String context = buildContext(relevantDocuments);

            // 3. 构建系统提示
            String systemPrompt = buildSystemPrompt(options.getSystemPrompt());

            // 4. 生成回答
            String response = chatClient.prompt()
                .system(systemPrompt)
                .user(buildUserPrompt(query, context))
                .model(options.getModelName())
                .call()
                .content();

            logger.debug("RAG query completed successfully. Query: {}, Response length: {}", 
                        query, response != null ? response.length() : 0);

            return response;

        } catch (Exception e) {
            logger.error("Error processing RAG query", e);
            throw new RuntimeException("Failed to process RAG query", e);
        }
    }

    @Override
    public String query(String query) {
        return query(query, QueryOptions.builder());
    }

    @Override
    public String generateContext(String query, QueryOptions options) {
        List<Document> relevantDocuments = searchSimilarDocuments(query, options);
        return buildContext(relevantDocuments);
    }

    @Override
    public DocumentStats getDocumentStats() {
        DocumentStats stats = new DocumentStats();
        
        try {
            // Spring AI VectorStore 可能没有直接的统计方法
            // 这里需要根据具体实现来获取统计信息
            stats.setTotalDocuments(0); // 需要实际实现
            stats.setTotalSize(0);
            stats.setDocumentsByType(new HashMap<>());
            stats.setAdditionalStats(new HashMap<>());
            
            logger.debug("Retrieved document stats: {} documents", stats.getTotalDocuments());
        } catch (Exception e) {
            logger.error("Error getting document stats", e);
            // 返回空的统计信息而不是抛出异常
        }
        
        return stats;
    }

    /**
     * 构建上下文字符串
     * 
     * @param documents 相关文档列表
     * @return 构建的上下文
     */
    private String buildContext(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "";
        }

        StringBuilder contextBuilder = new StringBuilder();
        
        for (int i = 0; i < documents.size(); i++) {
            Document doc = documents.get(i);
            contextBuilder.append("文档 ").append(i + 1).append(":\n");
            contextBuilder.append(doc.getContent()).append("\n\n");
        }

        return contextBuilder.toString().trim();
    }

    /**
     * 构建系统提示
     * 
     * @param customSystemPrompt 自定义系统提示
     * @return 系统提示
     */
    private String buildSystemPrompt(String customSystemPrompt) {
        if (StringUtils.hasText(customSystemPrompt)) {
            return customSystemPrompt;
        }

        return """
            你是一个专业的AI助手，专门根据提供的上下文信息来回答用户的问题。
            
            请遵循以下规则：
            1. 仅基于提供的上下文信息来回答问题
            2. 如果上下文中没有相关信息，请明确说明
            3. 保持回答的准确性和相关性
            4. 使用清晰、简洁的语言
            5. 如果需要，可以引用具体的文档内容
            """;
    }

    /**
     * 构建用户提示
     * 
     * @param query 用户查询
     * @param context 上下文
     * @return 用户提示
     */
    private String buildUserPrompt(String query, String context) {
        return String.format("""
            基于以下上下文信息，请回答用户的问题：
            
            上下文信息：
            %s
            
            用户问题：%s
            
            请根据上下文信息提供准确、有用的回答。
            """, context, query);
    }
} 