package com.terra.framework.nova.rag.retrieval;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 检索选项
 * 配置检索过程的各种参数
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Data
@Builder
public class RetrievalOptions {
    
    /**
     * 返回的最大结果数量
     */
    @Builder.Default
    private int topK = 4;
    
    /**
     * 是否重排序结果
     */
    @Builder.Default
    private boolean rerank = false;
    
    /**
     * 重排序模型ID（如果启用重排序）
     */
    private String rerankModelId;
    
    /**
     * 最小相似度阈值，低于此值的结果将被过滤
     */
    @Builder.Default
    private float minScoreThreshold = 0.0f;
    
    /**
     * 元数据过滤条件
     */
    @Builder.Default
    private Map<String, Object> filters = new HashMap<>();
    
    /**
     * 添加元数据过滤条件
     *
     * @param key 键
     * @param value 值
     * @return 当前选项对象
     */
    public RetrievalOptions addFilter(String key, Object value) {
        if (filters == null) {
            filters = new HashMap<>();
        }
        filters.put(key, value);
        return this;
    }
    
    /**
     * 创建默认检索选项
     *
     * @return 默认检索选项
     */
    public static RetrievalOptions defaults() {
        return RetrievalOptions.builder().build();
    }
    
    /**
     * 创建仅指定topK的检索选项
     *
     * @param topK 要返回的结果数量
     * @return 检索选项
     */
    public static RetrievalOptions of(int topK) {
        return RetrievalOptions.builder()
                .topK(topK)
                .build();
    }
} 