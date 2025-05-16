package com.terra.framework.nova.vector.search;

import com.terra.framework.nova.vector.document.Document;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 向量搜索结果
 *
 * @author terra-nova
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SearchResult {
    
    /**
     * 匹配的文档
     */
    private Document document;
    
    /**
     * 相似度分数
     */
    private float score;
    
    /**
     * 创建一个搜索结果
     *
     * @param document 文档
     * @param score 相似度分数
     * @return 搜索结果
     */
    public static SearchResult of(Document document, float score) {
        return SearchResult.builder()
                .document(document)
                .score(score)
                .build();
    }
    
    /**
     * 判断相似度是否高于指定阈值
     *
     * @param threshold 阈值
     * @return 是否高于阈值
     */
    public boolean isAboveThreshold(float threshold) {
        return score >= threshold;
    }
} 