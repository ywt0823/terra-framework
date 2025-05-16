package com.terra.framework.nova.vector.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 文档实体类，表示要向量化存储的文档
 *
 * @author terra-nova
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
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
     * 文档元数据
     */
    @Builder.Default
    private Map<String, Object> metadata = new HashMap<>();
    
    /**
     * 创建一个简单的文档
     *
     * @param id 文档ID
     * @param content 文档内容
     * @return 文档对象
     */
    public static Document of(String id, String content) {
        return Document.builder()
                .id(id)
                .content(content)
                .build();
    }
    
    /**
     * 创建一个带标题的文档
     *
     * @param id 文档ID
     * @param title 文档标题
     * @param content 文档内容
     * @return 文档对象
     */
    public static Document of(String id, String title, String content) {
        return Document.builder()
                .id(id)
                .title(title)
                .content(content)
                .build();
    }
    
    /**
     * 添加元数据
     *
     * @param key 元数据键
     * @param value 元数据值
     * @return 当前文档对象
     */
    public Document addMetadata(String key, Object value) {
        if (this.metadata == null) {
            this.metadata = new HashMap<>();
        }
        this.metadata.put(key, value);
        return this;
    }
}