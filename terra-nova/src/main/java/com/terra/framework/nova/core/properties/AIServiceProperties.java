package com.terra.framework.nova.core.properties;

import java.util.LinkedHashMap;
import java.util.Map;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI服务配置属性
 *
 * @author terra-nova
 */
@Data
@ConfigurationProperties(prefix = "terra.framework.ai")
public class AIServiceProperties {

    /**
     * 是否启用
     */
    private boolean enabled = true;

    /**
     * 默认模型ID
     */
    private String defaultModelId = "openai:gpt-3.5-turbo";

    /**
     * 模型配置
     */
    private Map<String, ModelProperties> models = new LinkedHashMap<>();

    /**
     * 模型属性
     */
    @Data
    public static class ModelProperties {

        /**
         * 模型类型
         */
        private String type;

        /**
         * 端点URL
         */
        private String endpoint;

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * API密钥ID
         */
        private String apiKeyId;

        /**
         * API密钥密钥
         */
        private String apiKeySecret;

        /**
         * 认证令牌
         */
        private String authToken;

        /**
         * 认证类型
         */
        private String authType;

        /**
         * 组织ID
         */
        private String organizationId;

        /**
         * 项目ID
         */
        private String projectId;

        /**
         * 超时时间（毫秒）
         */
        private int timeout = 30000;

        /**
         * 是否支持流式输出
         */
        private boolean streamSupport = true;

        /**
         * 默认参数
         */
        private Map<String, Object> defaultParameters = new LinkedHashMap<>();

        /**
         * 重试配置
         */
        private RetryProperties retry = new RetryProperties();
    }

    /**
     * 重试属性
     */
    @Data
    public static class RetryProperties {

        /**
         * 最大重试次数
         */
        private int maxRetries = 3;

        /**
         * 初始重试延迟（毫秒）
         */
        private long initialDelayMs = 1000;

        /**
         * 最大重试延迟（毫秒）
         */
        private long maxDelayMs = 10000;

        /**
         * 退避乘数
         */
        private double backoffMultiplier = 2.0;

        /**
         * 可重试的错误类型
         */
        private String[] retryableErrors = {
            "timeout",
            "rate_limit_exceeded",
            "server_error",
            "service_unavailable"
        };
    }
}
