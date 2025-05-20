package com.terra.framework.nova.rag.retrieval.rerank;

import com.terra.framework.nova.rag.document.Document;

import java.util.List;
import java.util.Map;

/**
 * 文档重排序器接口
 * 对检索到的文档进行更精准的排序，提高相关性
 *
 * @author Terra Framework Team
 * @date 2025年6月15日
 */
public interface Reranker {
    
    /**
     * 对检索到的文档列表进行重排序
     *
     * @param documents 待重排序的文档列表
     * @param query 原始查询
     * @return 重排序后的文档列表
     */
    List<RerankedDocument> rerank(List<Document> documents, String query);
    
    /**
     * 对检索到的文档列表进行重排序，支持额外参数
     *
     * @param documents 待重排序的文档列表
     * @param query 原始查询
     * @param parameters 额外参数
     * @return 重排序后的文档列表
     */
    List<RerankedDocument> rerank(List<Document> documents, String query, Map<String, Object> parameters);
    
    /**
     * 获取重排序器名称
     *
     * @return 重排序器名称
     */
    String getName();
    
    /**
     * 获取重排序器描述
     *
     * @return 重排序器描述
     */
    String getDescription();
} 