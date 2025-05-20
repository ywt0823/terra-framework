package com.terra.framework.nova.rag.retrieval.impl;

import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.embedding.EmbeddingService;
import com.terra.framework.nova.rag.properties.RAGProperties;
import com.terra.framework.nova.rag.retrieval.RetrievalOptions;
import com.terra.framework.nova.rag.retrieval.Retriever;
import com.terra.framework.nova.rag.storage.SearchResult;
import com.terra.framework.nova.rag.storage.VectorStore;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 默认检索器实现
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Slf4j
public class DefaultRetriever implements Retriever {
    
    private final VectorStore vectorStore;
    private final EmbeddingService embeddingService;
    private final RAGProperties properties;
    
    /**
     * 创建检索器
     *
     * @param vectorStore 向量存储
     * @param embeddingService 嵌入服务
     * @param properties RAG配置
     */
    public DefaultRetriever(VectorStore vectorStore, EmbeddingService embeddingService, RAGProperties properties) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
        this.properties = properties;
    }
    
    /**
     * 获取向量存储
     *
     * @return 向量存储
     */
    public VectorStore getVectorStore() {
        return vectorStore;
    }
    
    @Override
    public List<Document> retrieve(String query, int topK) {
        RetrievalOptions options = RetrievalOptions.builder()
                .topK(topK)
                .minScoreThreshold((float)properties.getRetrieval().getMinimumScore())
                .rerank(properties.getRetrieval().isRerank())
                .rerankModelId(properties.getRetrieval().getRerankModel())
                .build();
        
        return retrieve(query, options);
    }
    
    @Override
    public List<Document> retrieve(String query, RetrievalOptions options) {
        if (query == null || query.trim().isEmpty()) {
            log.warn("查询为空，无法检索");
            return Collections.emptyList();
        }
        
        try {
            // 创建查询的嵌入向量
            float[] queryEmbedding = embeddingService.createEmbedding(query);
            
            // 执行相似度搜索
            List<SearchResult> searchResults = vectorStore.similaritySearch(
                    queryEmbedding,
                    options.getTopK(),
                    options.getFilters());
            
            // 过滤低于阈值的结果
            List<Document> documents = searchResults.stream()
                    .filter(result -> result.getScore() >= options.getMinScoreThreshold())
                    .map(SearchResult::getDocument)
                    .collect(Collectors.toList());
            
            // 如果开启了重排序，对结果进行重排序
            if (options.isRerank() && !documents.isEmpty() && options.getRerankModelId() != null) {
                documents = rerankDocuments(query, documents, options.getRerankModelId());
            }
            
            log.debug("检索到 {} 个文档，查询: {}", documents.size(), query);
            return documents;
        } catch (Exception e) {
            log.error("检索过程中发生错误: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }
    
    /**
     * 重排序文档
     * 根据查询与文档的语义相关性对文档进行重新排序
     *
     * @param query 查询文本
     * @param documents 待重排序的文档
     * @param rerankModelId 重排序模型ID
     * @return 重排序后的文档
     */
    private List<Document> rerankDocuments(String query, List<Document> documents, String rerankModelId) {
        // 目前简单实现，后续可整合专门的重排序模型
        log.info("执行重排序，模型ID: {}", rerankModelId);
        return documents;
    }
} 