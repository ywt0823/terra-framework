package com.terra.framework.nova.rag.document;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 文档的简单实现
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Data
public class SimpleDocument implements Document {
    
    private String id;
    private String content;
    private Map<String, Object> metadata;
    
    /**
     * 创建一个新文档，自动生成ID
     *
     * @param content 文档内容
     */
    public SimpleDocument(String content) {
        this(UUID.randomUUID().toString(), content, new HashMap<>());
    }
    
    /**
     * 创建一个新文档，自动生成ID
     *
     * @param content 文档内容
     * @param metadata 元数据
     */
    public SimpleDocument(String content, Map<String, Object> metadata) {
        this(UUID.randomUUID().toString(), content, metadata);
    }
    
    /**
     * 创建一个新文档
     *
     * @param id 文档ID
     * @param content 文档内容
     * @param metadata 元数据
     */
    public SimpleDocument(String id, String content, Map<String, Object> metadata) {
        this.id = id;
        this.content = content;
        this.metadata = metadata != null ? metadata : new HashMap<>();
    }
    
    @Override
    public void addMetadata(String key, Object value) {
        if (metadata == null) {
            metadata = new HashMap<>();
        }
        metadata.put(key, value);
    }
    
    /**
     * 创建文档构建器
     *
     * @return 文档构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * 文档构建器
     */
    public static class Builder {
        private String id;
        private String content;
        private final Map<String, Object> metadata = new HashMap<>();
        
        public Builder id(String id) {
            this.id = id;
            return this;
        }
        
        public Builder content(String content) {
            this.content = content;
            return this;
        }
        
        public Builder metadata(Map<String, Object> metadata) {
            if (metadata != null) {
                this.metadata.putAll(metadata);
            }
            return this;
        }
        
        public Builder addMetadata(String key, Object value) {
            this.metadata.put(key, value);
            return this;
        }
        
        public SimpleDocument build() {
            if (id == null) {
                id = UUID.randomUUID().toString();
            }
            return new SimpleDocument(id, content, metadata);
        }
    }
} 