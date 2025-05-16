package com.terra.framework.nova.llm.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * LLM配置属性
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.llm")
public class LLMProperties {

    /**
     * 是否启用LLM功能
     */
    private boolean enabled = true;

    /**
     * DeepSeek模型配置
     */
    private DeepSeekConfig deepseek = new DeepSeekConfig();

    /**
     * 通义千问模型配置
     */
    private TongyiConfig tongyi = new TongyiConfig();

    /**
     * Dify.AI配置
     */
    private DifyConfig dify = new DifyConfig();

    @Data
    public static class DeepSeekConfig {
        /**
         * API密钥
         */
        private String apiKey;

        /**
         * API端点
         */
        private String apiEndpoint;

        /**
         * 模型名称
         */
        private String modelName = "deepseek-chat";

        /**
         * 温度参数 (0.0-1.0)
         */
        private Double temperature = 0.7;

        /**
         * 最大token数
         */
        private Integer maxTokens = 2048;

        /**
         * 超时时间(毫秒)
         */
        private Long timeoutMs = 30000L;

        /**
         * 重试次数
         */
        private Integer maxRetries = 3;

        /**
         * 额外参数
         */
        private Map<String, Object> extraParams = new HashMap<>();
    }

    @Data
    public static class TongyiConfig {
        /**
         * API密钥
         */
        private String apiKey;

        /**
         * API端点
         */
        private String apiEndpoint;

        /**
         * 模型名称
         */
        private String modelName;

        /**
         * 温度参数 (0.0-1.0)
         */
        private Double temperature = 0.7;

        /**
         * 最大token数
         */
        private Integer maxTokens = 2048;

        /**
         * 超时时间(毫秒)
         */
        private Long timeoutMs = 30000L;

        /**
         * 重试次数
         */
        private Integer maxRetries = 3;

        /**
         * 额外参数
         */
        private Map<String, Object> extraParams = new HashMap<>();
    }

    @Data
    public static class DifyConfig {
        /**
         * API密钥
         */
        private String apiKey;

        /**
         * API端点
         */
        private String apiEndpoint = "https://api.dify.ai/v1";

        /**
         * 应用ID
         */
        private String appId;

        /**
         * 是否使用知识库
         */
        private boolean useKnowledgeBase = false;

        /**
         * 知识库ID
         */
        private String knowledgeBaseId;

        /**
         * 温度参数 (0.0-1.0)
         */
        private Double temperature = 0.7;

        /**
         * 最大token数
         */
        private Integer maxTokens = 2048;

        /**
         * 超时时间(毫秒)
         */
        private Long timeoutMs = 30000L;

        /**
         * 重试次数
         */
        private Integer maxRetries = 3;

        /**
         * 额外参数
         */
        private Map<String, Object> extraParams = new HashMap<>();
    }
}
