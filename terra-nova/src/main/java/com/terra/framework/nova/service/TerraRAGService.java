package com.terra.framework.nova.service;

import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;

import java.util.List;
import java.util.Map;

/**
 * Terra RAG 服务接口
 * 
 * <p>基于 Spring AI 的检索增强生成服务，提供：
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
public interface TerraRAGService {

    /**
     * 添加文档到向量存储
     * 
     * @param documents 文档列表
     */
    void addDocuments(List<Document> documents);

    /**
     * 添加单个文档到向量存储
     * 
     * @param document 文档
     */
    void addDocument(Document document);

    /**
     * 从文本创建并添加文档
     * 
     * @param text 文本内容
     * @param metadata 元数据
     */
    void addTextDocument(String text, Map<String, Object> metadata);

    /**
     * 删除文档
     * 
     * @param documentIds 文档ID列表
     */
    void deleteDocuments(List<String> documentIds);

    /**
     * 清空所有文档
     */
    void clearDocuments();

    /**
     * 搜索相似文档
     * 
     * @param query 查询文本
     * @param options 查询选项
     * @return 相似文档列表
     */
    List<Document> searchSimilarDocuments(String query, QueryOptions options);

    /**
     * 搜索相似文档（使用默认选项）
     * 
     * @param query 查询文本
     * @return 相似文档列表
     */
    List<Document> searchSimilarDocuments(String query);

    /**
     * RAG 查询 - 检索相关文档并生成回答
     * 
     * @param query 用户查询
     * @param options 查询选项
     * @return AI 生成的回答
     */
    String query(String query, QueryOptions options);

    /**
     * RAG 查询（使用默认选项）
     * 
     * @param query 用户查询
     * @return AI 生成的回答
     */
    String query(String query);

    /**
     * 生成上下文 - 基于查询检索相关文档并构建上下文
     * 
     * @param query 查询文本
     * @param options 查询选项
     * @return 构建的上下文
     */
    String generateContext(String query, QueryOptions options);

    /**
     * 获取文档统计信息
     * 
     * @return 统计信息
     */
    DocumentStats getDocumentStats();

    /**
     * 查询选项
     */
    class QueryOptions {
        private int maxResults = 5;
        private double similarityThreshold = 0.8;
        private Map<String, Object> filterExpression;
        private boolean includeMetadata = true;
        private String modelName;
        private String systemPrompt;

        // Getters and Setters
        public int getMaxResults() {
            return maxResults;
        }

        public void setMaxResults(int maxResults) {
            this.maxResults = maxResults;
        }

        public double getSimilarityThreshold() {
            return similarityThreshold;
        }

        public void setSimilarityThreshold(double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
        }

        public Map<String, Object> getFilterExpression() {
            return filterExpression;
        }

        public void setFilterExpression(Map<String, Object> filterExpression) {
            this.filterExpression = filterExpression;
        }

        public boolean isIncludeMetadata() {
            return includeMetadata;
        }

        public void setIncludeMetadata(boolean includeMetadata) {
            this.includeMetadata = includeMetadata;
        }

        public String getModelName() {
            return modelName;
        }

        public void setModelName(String modelName) {
            this.modelName = modelName;
        }

        public String getSystemPrompt() {
            return systemPrompt;
        }

        public void setSystemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
        }

        // Builder pattern
        public static QueryOptions builder() {
            return new QueryOptions();
        }

        public QueryOptions maxResults(int maxResults) {
            this.maxResults = maxResults;
            return this;
        }

        public QueryOptions similarityThreshold(double similarityThreshold) {
            this.similarityThreshold = similarityThreshold;
            return this;
        }

        public QueryOptions filterExpression(Map<String, Object> filterExpression) {
            this.filterExpression = filterExpression;
            return this;
        }

        public QueryOptions includeMetadata(boolean includeMetadata) {
            this.includeMetadata = includeMetadata;
            return this;
        }

        public QueryOptions modelName(String modelName) {
            this.modelName = modelName;
            return this;
        }

        public QueryOptions systemPrompt(String systemPrompt) {
            this.systemPrompt = systemPrompt;
            return this;
        }

        /**
         * 转换为 Spring AI SearchRequest
         * 
         * @return SearchRequest
         */
        public SearchRequest toSearchRequest() {
            SearchRequest.Builder builder = SearchRequest.builder()
                .topK(maxResults)
                .similarityThreshold(similarityThreshold);

            if (filterExpression != null) {
                // 根据Spring AI的实际API设置过滤表达式
                // builder.filterExpression(filterExpression);
            }

            return builder.build();
        }
    }

    /**
     * 文档统计信息
     */
    class DocumentStats {
        private long totalDocuments;
        private long totalSize;
        private Map<String, Long> documentsByType;
        private Map<String, Object> additionalStats;

        // Getters and Setters
        public long getTotalDocuments() {
            return totalDocuments;
        }

        public void setTotalDocuments(long totalDocuments) {
            this.totalDocuments = totalDocuments;
        }

        public long getTotalSize() {
            return totalSize;
        }

        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }

        public Map<String, Long> getDocumentsByType() {
            return documentsByType;
        }

        public void setDocumentsByType(Map<String, Long> documentsByType) {
            this.documentsByType = documentsByType;
        }

        public Map<String, Object> getAdditionalStats() {
            return additionalStats;
        }

        public void setAdditionalStats(Map<String, Object> additionalStats) {
            this.additionalStats = additionalStats;
        }
    }
} 