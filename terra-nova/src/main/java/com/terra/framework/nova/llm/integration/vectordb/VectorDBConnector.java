package com.terra.framework.nova.llm.integration.vectordb;

import java.util.List;

/**
 * 向量数据库连接器接口
 */
public interface VectorDBConnector {

    /**
     * 连接到向量数据库
     */
    void connect();

    /**
     * 关闭连接
     */
    void close();

    /**
     * 检索相似向量
     *
     * @param query 查询文本
     * @param maxResults 最大结果数量
     * @return 相关文档列表
     */
    List<Document> search(String query, int maxResults);

    /**
     * 添加文档
     *
     * @param document 要添加的文档
     */
    void addDocument(Document document);

    /**
     * 批量添加文档
     *
     * @param documents 要添加的文档列表
     */
    void addDocuments(List<Document> documents);

    /**
     * 删除文档
     *
     * @param documentId 文档ID
     */
    void deleteDocument(String documentId);
} 