package com.terra.framework.nova.rag.retrieval.impl;

import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.embedding.EmbeddingService;
import com.terra.framework.nova.rag.properties.RAGProperties;
import com.terra.framework.nova.rag.retrieval.RetrievalOptions;
import com.terra.framework.nova.rag.retrieval.Retriever;
import com.terra.framework.nova.rag.retrieval.rerank.RerankedDocument;
import com.terra.framework.nova.rag.retrieval.rerank.Reranker;
import com.terra.framework.nova.rag.storage.SearchResult;
import com.terra.framework.nova.rag.storage.VectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;

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
    private final ObjectProvider<Reranker> rerankerProvider;
    
    /**
     * 创建检索器
     *
     * @param vectorStore 向量存储
     * @param embeddingService 嵌入服务
     * @param properties RAG配置
     * @param rerankerProvider 重排序器提供者(可选)
     */
    public DefaultRetriever(
            VectorStore vectorStore, 
            EmbeddingService embeddingService, 
            RAGProperties properties,
            ObjectProvider<Reranker> rerankerProvider) {
        this.vectorStore = vectorStore;
        this.embeddingService = embeddingService;
        this.properties = properties;
        this.rerankerProvider = rerankerProvider;
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
            if (options.isRerank() && !documents.isEmpty()) {
                documents = rerankDocuments(query, documents, options);
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
     * @param options 检索选项
     * @return 重排序后的文档
     */
    private List<Document> rerankDocuments(String query, List<Document> documents, RetrievalOptions options) {
        Reranker reranker = rerankerProvider.getIfAvailable();
        if (reranker == null) {
            log.info("未找到Reranker实现，跳过重排序");
            return documents;
        }
        
        try {
            log.info("使用 {} 重排序器对 {} 个文档进行重排序", reranker.getName(), documents.size());
            List<RerankedDocument> rerankedDocs = reranker.rerank(documents, query);
            
            // 过滤低分文档（如果有阈值设置）
            if (options.getMinScoreThreshold() > 0) {
                rerankedDocs = rerankedDocs.stream()
                        .filter(doc -> doc.getScore() >= options.getMinScoreThreshold())
                        .collect(Collectors.toList());
            }
            
            // 将重排序后的文档转回Document列表
            return rerankedDocs.stream()
                    .map(RerankedDocument::getDocument)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("重排序过程中发生错误: {}", e.getMessage(), e);
            return documents; // 出错时返回原始文档列表
        }
    }
} 