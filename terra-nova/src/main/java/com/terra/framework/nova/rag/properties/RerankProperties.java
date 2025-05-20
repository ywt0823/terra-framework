package com.terra.framework.nova.rag.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * 重排序配置属性
 *
 * @author Terra Framework Team
 * @date 2025年6月15日
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.rag.rerank")
public class RerankProperties {
    
    /**
     * 是否启用重排序
     */
    private boolean enabled = false;
    
    /**
     * 重排序器类型：cross-encoder, llm
     */
    private String type = "cross-encoder";
    
    /**
     * 模型ID
     */
    private String modelId = "";
    
    /**
     * 最低分数阈值，低于该值的文档将被过滤
     */
    private double threshold = 0.0;
    
    /**
     * 最大重排序文档数量
     */
    private int maxDocuments = 50;
    
    /**
     * 交叉编码器配置
     */
    private CrossEncoder crossEncoder = new CrossEncoder();
    
    /**
     * LLM重排序配置
     */
    private LLMRerank llmRerank = new LLMRerank();
    
    /**
     * 交叉编码器配置
     */
    @Data
    public static class CrossEncoder {
        /**
         * 交叉编码器模型名称
         */
        private String modelName = "ms-marco-MiniLM-L-6-v2";
        
        /**
         * 批处理大小
         */
        private int batchSize = 16;
    }
    
    /**
     * LLM重排序配置
     */
    @Data
    public static class LLMRerank {
        /**
         * 提示词模板
         */
        private String promptTemplate = "请评估以下文档与问题\"{query}\"的相关性，返回0到100之间的相关性分数。\n\n文档内容：{content}";
        
        /**
         * 温度参数
         */
        private double temperature = 0.0;
        
        /**
         * 重排序参数
         */
        private Map<String, Object> parameters = new HashMap<>();
    }
} 