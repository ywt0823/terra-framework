package com.terra.framework.nova.rag.retrieval.rerank.impl;

import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.retrieval.rerank.RerankedDocument;
import com.terra.framework.nova.rag.retrieval.rerank.Reranker;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 抽象重排序器
 * 提供基本功能实现，具体重排序逻辑由子类实现
 *
 * @author Terra Framework Team
 * @date 2025年6月15日
 */
public abstract class AbstractReranker implements Reranker {

    /**
     * 抽象方法，计算文档与查询的相关性分数
     *
     * @param document 文档
     * @param query 查询
     * @param parameters 参数
     * @return 相关性分数
     */
    protected abstract double scoreDocument(Document document, String query, Map<String, Object> parameters);
    
    @Override
    public List<RerankedDocument> rerank(List<Document> documents, String query) {
        return rerank(documents, query, Collections.emptyMap());
    }
    
    @Override
    public List<RerankedDocument> rerank(List<Document> documents, String query, Map<String, Object> parameters) {
        if (documents == null || documents.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 计算每个文档的分数并排序
        return documents.stream()
                .map(doc -> RerankedDocument.of(doc, scoreDocument(doc, query, parameters)))
                .sorted(Comparator.comparingDouble(RerankedDocument::getScore).reversed())
                .collect(Collectors.toList());
    }
    
    /**
     * 过滤低分文档
     *
     * @param documents 文档列表
     * @param threshold 分数阈值
     * @return 过滤后的文档列表
     */
    protected List<RerankedDocument> filterByThreshold(List<RerankedDocument> documents, double threshold) {
        if (threshold <= 0) {
            return documents;
        }
        
        return documents.stream()
                .filter(doc -> doc.getScore() >= threshold)
                .collect(Collectors.toList());
    }
} 