package com.terra.framework.nova.rag.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 嵌入模型配置属性
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.rag.embedding")
public class EmbeddingProperties {

    /**
     * 是否启用嵌入功能
     */
    private boolean enabled = true;

    /**
     * 默认使用的嵌入模型ID
     */
    private String modelId = "text-embedding";

    /**
     * 默认使用的嵌入模型类型
     */
    private String modelType = "deepseek";

    /**
     * 嵌入向量维度
     */
    private int dimension = 1024;

    /**
     * 批处理大小
     */
    private int batchSize = 20;

    /**
     * 缓存是否启用
     */
    private boolean cacheEnabled = true;

    /**
     * 缓存大小
     */
    private int cacheSize = 1000;

    /**
     * 缓存过期时间（秒）
     */
    private long cacheTtlSeconds = 3600;
}
