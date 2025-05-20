package com.terra.framework.nova.rag.storage;

import com.terra.framework.nova.rag.document.Document;
import lombok.AllArgsConstructor;
import lombok.Data;

/**
 * 向量搜索结果
 * 包含文档和相似度得分
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Data
@AllArgsConstructor
public class SearchResult {
    
    /**
     * 匹配的文档
     */
    private Document document;
    
    /**
     * 相似度得分，值越大表示越相似
     */
    private float score;
    
    /**
     * 获取文档ID
     *
     * @return 文档ID
     */
    public String getDocumentId() {
        return document != null ? document.getId() : null;
    }
    
    /**
     * 获取文档内容
     *
     * @return 文档内容
     */
    public String getContent() {
        return document != null ? document.getContent() : null;
    }
    
    /**
     * 创建搜索结果
     *
     * @param document 文档
     * @param score 相似度得分
     * @return 搜索结果
     */
    public static SearchResult of(Document document, float score) {
        return new SearchResult(document, score);
    }
} 