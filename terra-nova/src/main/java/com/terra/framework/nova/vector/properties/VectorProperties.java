package com.terra.framework.nova.vector.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 向量存储配置属性
 *
 * @author terra-nova
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.vector")
public class VectorProperties {

    /**
     * 是否启用向量存储
     */
    private boolean enabled = true;

    /**
     * 嵌入模型名称
     */
    private String embeddingModel = "text-embedding-3-small";

    /**
     * 批处理大小
     */
    private int batchSize = 100;

    /**
     * 文档块大小
     */
    private int chunkSize = 1000;

    /**
     * 文档块重叠
     */
    private int chunkOverlap = 200;

    /**
     * 相似度阈值
     */
    private float similarityThreshold = 0.7f;

    /**
     * Redis配置
     */
    private RedisProperties redis = new RedisProperties();

    /**
     * Redis配置属性
     */
    @Data
    public static class RedisProperties {
        /**
         * Redis主机
         */
        private String host = "localhost";

        /**
         * Redis端口
         */
        private int port = 6379;

        /**
         * Redis密码
         */
        private String password;

        /**
         * Redis数据库索引
         */
        private int database = 0;

        /**
         * 连接超时时间（毫秒）
         */
        private int timeout = 2000;

        /**
         * 最大连接数
         */
        private int maxTotal = 8;

        /**
         * 最大空闲连接数
         */
        private int maxIdle = 8;

        /**
         * 最小空闲连接数
         */
        private int minIdle = 0;

        /**
         * 向量键前缀
         */
        private String vectorKeyPrefix = "nova:vector:";

        /**
         * 元数据键前缀
         */
        private String metadataKeyPrefix = "nova:metadata:";
    }
}
