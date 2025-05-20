package com.terra.framework.nova.rag.retrieval;

import com.terra.framework.nova.rag.document.Document;

import java.util.List;

/**
 * 检索器接口
 * 负责根据查询从知识库中检索相关文档
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public interface Retriever {
    
    /**
     * 检索与查询相关的文档
     *
     * @param query 查询文本
     * @param topK 返回的最大结果数
     * @return 相关文档列表，按相关性降序排列
     */
    List<Document> retrieve(String query, int topK);
    
    /**
     * 使用自定义选项检索与查询相关的文档
     *
     * @param query 查询文本
     * @param options 检索选项
     * @return 相关文档列表，按相关性降序排列
     */
    List<Document> retrieve(String query, RetrievalOptions options);
    
    /**
     * 使用默认选项检索与查询相关的文档
     *
     * @param query 查询文本
     * @return 相关文档列表，按相关性降序排列
     */
    default List<Document> retrieve(String query) {
        return retrieve(query, RetrievalOptions.defaults());
    }
} 