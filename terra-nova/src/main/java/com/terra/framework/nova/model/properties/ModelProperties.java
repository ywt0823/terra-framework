package com.terra.framework.nova.model.properties;

import com.terra.framework.nova.model.router.RoutingStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 模型配置属性
 *
 * @author terra-nova
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.model")
public class ModelProperties {

    /**
     * 是否启用模型功能
     */
    private boolean enabled = true;

    /**
     * 默认提供商
     */
    private String defaultProvider = "openai";

    /**
     * 默认模型
     */
    private String defaultModel = "gpt-3.5-turbo";

    /**
     * 路由策略
     */
    private RoutingStrategy routingStrategy = RoutingStrategy.USER_PREFERRED;

    /**
     * 是否启用回退策略
     */
    private boolean fallbackEnabled = true;

    /**
     * OpenAI配置
     */
    private OpenAIConfig openai = new OpenAIConfig();

    /**
     * Ollama配置
     */
    private OllamaConfig ollama = new OllamaConfig();

    /**
     * OpenAI配置类
     */
    @Data
    public static class OpenAIConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * API密钥
         */
        private String apiKey;

        /**
         * 组织ID
         */
        private String organization;

        /**
         * 请求超时时间（秒）
         */
        private int timeout = 30;
    }

    /**
     * Ollama配置类
     */
    @Data
    public static class OllamaConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;

        /**
         * 基础URL
         */
        private String baseUrl = "http://localhost:11434";

        /**
         * 请求超时时间（秒）
         */
        private int timeout = 60;
    }
}
