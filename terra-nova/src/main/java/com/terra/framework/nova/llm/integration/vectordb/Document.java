package com.terra.framework.nova.llm.integration.vectordb;

import lombok.Builder;
import lombok.Data;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文档实体类
 */
@Data
@Builder
public class Document {

    /**
     * 文档ID
     */
    private String id;

    /**
     * 文档内容
     */
    private String content;

    /**
     * 文档标题
     */
    private String title;

    /**
     * 文档向量
     */
    private List<Float> vector;

    /**
     * 文档来源
     */
    private String source;

    /**
     * 元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
} 