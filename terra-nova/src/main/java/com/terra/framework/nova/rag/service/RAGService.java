package com.terra.framework.nova.rag.service;

import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.exception.DocumentLoadException;
import com.terra.framework.nova.rag.retrieval.RetrievalOptions;

import java.util.List;
import java.util.Map;

/**
 * RAG服务接口
 * 提供检索增强生成的核心功能
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public interface RAGService {
    
    /**
     * 添加文档到知识库
     *
     * @param document 文档
     * @return 是否添加成功
     */
    boolean addDocument(Document document);
    
    /**
     * 批量添加文档到知识库
     *
     * @param documents 文档列表
     * @return 添加成功的文档数量
     */
    int addDocuments(List<Document> documents);
    
    /**
     * 从指定来源加载文档到知识库
     *
     * @param source 文档来源，可以是文件路径、URL等
     * @return 添加成功的文档数量
     * @throws DocumentLoadException 文档加载异常
     */
    int loadDocuments(String source) throws DocumentLoadException;
    
    /**
     * 从多个来源加载文档到知识库
     *
     * @param sources 文档来源列表
     * @return 添加成功的文档数量
     * @throws DocumentLoadException 文档加载异常
     */
    int loadDocuments(List<String> sources) throws DocumentLoadException;
    
    /**
     * 检索与查询相关的文档
     *
     * @param query 查询文本
     * @param topK 返回的最大结果数
     * @return 相关文档列表
     */
    List<Document> retrieve(String query, int topK);
    
    /**
     * 使用自定义选项检索与查询相关的文档
     *
     * @param query 查询文本
     * @param options 检索选项
     * @return 相关文档列表
     */
    List<Document> retrieve(String query, RetrievalOptions options);
    
    /**
     * 生成查询的上下文
     *
     * @param query 查询文本
     * @param topK 使用的文档数量
     * @return 上下文文本
     */
    String generateContext(String query, int topK);
    
    /**
     * 生成查询的上下文，使用自定义参数
     *
     * @param query 查询文本
     * @param options 检索选项
     * @param parameters 上下文生成参数
     * @return 上下文文本
     */
    String generateContext(String query, RetrievalOptions options, Map<String, Object> parameters);
    
    /**
     * 从知识库中删除文档
     *
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    boolean removeDocument(String documentId);
    
    /**
     * 从知识库中批量删除文档
     *
     * @param documentIds 文档ID列表
     * @return 删除成功的文档数量
     */
    int removeDocuments(List<String> documentIds);
    
    /**
     * 清空知识库中的所有文档
     */
    void clearAll();
} 