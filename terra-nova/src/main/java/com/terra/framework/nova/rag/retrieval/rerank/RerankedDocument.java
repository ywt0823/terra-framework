package com.terra.framework.nova.rag.retrieval.rerank;

import com.terra.framework.nova.rag.document.Document;
import lombok.Data;
import lombok.Builder;

import java.util.Map;

/**
 * 重排序文档
 * 包含文档和相关性分数信息
 *
 * @author Terra Framework Team
 * @date 2025年6月15日
 */
@Data
@Builder
public class RerankedDocument {
    
    /**
     * 原始文档
     */
    private Document document;
    
    /**
     * 相关性分数
     */
    private double score;
    
    /**
     * 创建重排序文档对象
     *
     * @param document 原始文档
     * @param score 相关性分数
     * @return 重排序文档对象
     */
    public static RerankedDocument of(Document document, double score) {
        return RerankedDocument.builder()
                .document(document)
                .score(score)
                .build();
    }
    
    /**
     * 获取原始文档内容
     *
     * @return 文档内容
     */
    public String getContent() {
        return document.getContent();
    }
    
    /**
     * 获取原始文档ID
     *
     * @return 文档ID
     */
    public String getId() {
        return document.getId();
    }
    
    /**
     * 获取原始文档元数据
     *
     * @return 文档元数据
     */
    public Map<String, Object> getMetadata() {
        return document.getMetadata();
    }
} 