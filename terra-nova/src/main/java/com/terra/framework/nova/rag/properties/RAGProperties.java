package com.terra.framework.nova.rag.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * RAG模块配置属性
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.rag")
public class RAGProperties {
    
    /**
     * 是否启用RAG
     */
    private boolean enabled = true;
    
    /**
     * 分割配置
     */
    private Splitting splitting = new Splitting();
    
    /**
     * 上下文配置
     */
    private Context context = new Context();
    
    /**
     * 检索配置
     */
    private Retrieval retrieval = new Retrieval();
    
    /**
     * 向量存储配置
     */
    private VectorStore vectorStore = new VectorStore();
    
    /**
     * 文档分割配置
     */
    @Data
    public static class Splitting {
        /**
         * 分块大小
         */
        private int chunkSize = 1000;
        
        /**
         * 块重叠大小
         */
        private int overlap = 200;
        
        /**
         * 分割器类型
         */
        private String splitter = "character";
    }
    
    /**
     * 上下文构建配置
     */
    @Data
    public static class Context {
        /**
         * 上下文模板
         */
        private String template = "根据以下上下文回答问题:\n\n{context}\n\n问题: {question}";
        
        /**
         * 最大Token数
         */
        private int maxTokens = 3500;
        
        /**
         * 是否格式化文档
         */
        private boolean formatDocuments = true;
        
        /**
         * 文档格式化模板
         */
        private String documentTemplate = "文档[{index}]: {content}\n来源: {source}";
    }
    
    /**
     * 检索配置
     */
    @Data
    public static class Retrieval {
        /**
         * 返回的最大结果数
         */
        private int topK = 5;
        
        /**
         * 是否重排序
         */
        private boolean rerank = false;
        
        /**
         * 重排序模型
         */
        private String rerankModel = "";
        
        /**
         * 最低相似度阈值
         */
        private double minimumScore = 0.7;
    }
    
    /**
     * 向量存储配置
     */
    @Data
    public static class VectorStore {
        /**
         * 存储类型
         */
        private String type = "in-memory";
        
        /**
         * 连接URL
         */
        private String url = "localhost:19530";
        
        /**
         * 集合名称
         */
        private String collectionName = "documents";
    }
} 