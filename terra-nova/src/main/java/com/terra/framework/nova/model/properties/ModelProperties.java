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
     * 健康监控配置
     */
    private HealthMonitorConfig healthMonitor = new HealthMonitorConfig();
    
    /**
     * 负载均衡配置
     */
    private LoadBalancerConfig loadBalancer = new LoadBalancerConfig();

    /**
     * OpenAI配置
     */
    private OpenAIConfig openai = new OpenAIConfig();

    /**
     * Ollama配置
     */
    private OllamaConfig ollama = new OllamaConfig();
    
    /**
     * Azure OpenAI配置
     */
    private AzureOpenAIConfig azure = new AzureOpenAIConfig();
    
    /**
     * Anthropic配置
     */
    private AnthropicConfig anthropic = new AnthropicConfig();

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
    
    /**
     * Azure OpenAI配置类
     */
    @Data
    public static class AzureOpenAIConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * 资源名称
         */
        private String resourceName;
        
        /**
         * 部署ID
         */
        private String deploymentId;
        
        /**
         * API版本
         */
        private String apiVersion = "2023-12-01-preview";
        
        /**
         * API密钥
         */
        private String apiKey;
        
        /**
         * 请求超时时间（秒）
         */
        private int timeout = 30;
    }
    
    /**
     * Anthropic配置类
     */
    @Data
    public static class AnthropicConfig {
        /**
         * 是否启用
         */
        private boolean enabled = true;
        
        /**
         * API密钥
         */
        private String apiKey;
        
        /**
         * 默认模型
         */
        private String defaultModel = "claude-3-opus-20240229";
        
        /**
         * 请求超时时间（秒）
         */
        private int timeout = 45;
    }
    
    /**
     * 健康监控配置类
     */
    @Data
    public static class HealthMonitorConfig {
        /**
         * 是否启用健康监控
         */
        private boolean enabled = true;
        
        /**
         * 监控间隔（秒）
         */
        private int intervalSeconds = 300;
        
        /**
         * 每个客户端的最大历史记录数
         */
        private int maxHistoryPerClient = 100;
        
        /**
         * 自动开始监控所有客户端
         */
        private boolean autoStartMonitoring = true;
    }
    
    /**
     * 负载均衡配置类
     */
    @Data
    public static class LoadBalancerConfig {
        /**
         * 是否启用负载均衡
         */
        private boolean enabled = true;
        
        /**
         * 负载均衡策略
         */
        private LoadBalancerStrategy strategy = LoadBalancerStrategy.ROUND_ROBIN;
        
        /**
         * 是否考虑健康状态
         */
        private boolean considerHealth = true;
    }
    
    /**
     * 负载均衡策略枚举
     */
    public enum LoadBalancerStrategy {
        /**
         * 轮询策略
         */
        ROUND_ROBIN,
        
        /**
         * 最小延迟策略
         */
        LEAST_LATENCY
    }
}
