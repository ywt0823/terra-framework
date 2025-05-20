package com.terra.framework.nova.rag.storage;

import com.terra.framework.nova.rag.document.Document;

import java.util.List;
import java.util.Map;

/**
 * 向量存储接口
 * 负责存储和检索文档的向量表示
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public interface VectorStore {
    
    /**
     * 添加文档及其向量到存储
     *
     * @param documents 文档列表
     * @param embeddings 对应的嵌入向量列表
     */
    void addDocuments(List<Document> documents, List<float[]> embeddings);
    
    /**
     * 添加单个文档及其向量到存储
     *
     * @param document 文档
     * @param embedding 嵌入向量
     */
    default void addDocument(Document document, float[] embedding) {
        addDocuments(List.of(document), List.of(embedding));
    }
    
    /**
     * 根据向量相似度搜索文档
     *
     * @param queryEmbedding 查询向量
     * @param topK 返回的最大结果数
     * @return 搜索结果列表，按相似度降序排列
     */
    List<SearchResult> similaritySearch(float[] queryEmbedding, int topK);
    
    /**
     * 根据向量相似度和过滤条件搜索文档
     *
     * @param queryEmbedding 查询向量
     * @param topK 返回的最大结果数
     * @param filter 过滤条件，键为元数据字段名，值为匹配值
     * @return 搜索结果列表，按相似度降序排列
     */
    List<SearchResult> similaritySearch(float[] queryEmbedding, int topK, Map<String, Object> filter);
    
    /**
     * 从存储中删除文档
     *
     * @param ids 要删除的文档ID列表
     */
    void deleteDocuments(List<String> ids);
    
    /**
     * 从存储中删除单个文档
     *
     * @param id 要删除的文档ID
     */
    default void deleteDocument(String id) {
        deleteDocuments(List.of(id));
    }
    
    /**
     * 清空存储中的所有文档
     */
    void clear();
    
    /**
     * 获取存储中的文档数量
     *
     * @return 文档数量
     */
    int size();
} 