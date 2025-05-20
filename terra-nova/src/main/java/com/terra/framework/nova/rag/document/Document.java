package com.terra.framework.nova.rag.document;

import java.util.Map;

/**
 * 表示可被索引和检索的文档
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public interface Document {
    
    /**
     * 获取文档唯一标识
     *
     * @return 文档ID
     */
    String getId();
    
    /**
     * 获取文档内容
     *
     * @return 文档内容
     */
    String getContent();
    
    /**
     * 获取文档元数据
     *
     * @return 元数据Map
     */
    Map<String, Object> getMetadata();
    
    /**
     * 设置文档ID
     *
     * @param id 文档ID
     */
    void setId(String id);
    
    /**
     * 设置文档内容
     *
     * @param content 文档内容
     */
    void setContent(String content);
    
    /**
     * 设置文档元数据
     *
     * @param metadata 元数据Map
     */
    void setMetadata(Map<String, Object> metadata);
    
    /**
     * 添加元数据
     *
     * @param key 键
     * @param value 值
     */
    void addMetadata(String key, Object value);
} 