package com.terra.framework.nova.manager;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Terra 模型管理器接口
 * 
 * <p>定义模型管理的核心功能，包括：
 * <ul>
 *   <li>模型注册和发现</li>
 *   <li>模型配置管理</li>
 *   <li>模型状态监控</li>
 *   <li>负载均衡和故障转移</li>
 * </ul>
 * 
 * @author terra-nova
 * @since 0.0.1
 */
public interface TerraModelManager {

    /**
     * 获取默认模型名称
     * 
     * @return 默认模型名称
     */
    String getDefaultModelName();

    /**
     * 获取所有可用的模型名称
     * 
     * @return 模型名称列表
     */
    List<String> getAvailableModels();

    /**
     * 检查指定模型是否可用
     * 
     * @param modelName 模型名称
     * @return true 如果模型可用
     */
    boolean isModelAvailable(String modelName);

    /**
     * 获取模型配置信息
     * 
     * @param modelName 模型名称
     * @return 模型配置信息
     */
    Optional<ModelInfo> getModelInfo(String modelName);

    /**
     * 获取所有模型的配置信息
     * 
     * @return 模型配置信息映射
     */
    Map<String, ModelInfo> getAllModelInfo();

    /**
     * 注册模型
     * 
     * @param modelName 模型名称
     * @param modelInfo 模型信息
     */
    void registerModel(String modelName, ModelInfo modelInfo);

    /**
     * 注销模型
     * 
     * @param modelName 模型名称
     */
    void unregisterModel(String modelName);

    /**
     * 获取模型健康状态
     * 
     * @param modelName 模型名称
     * @return 健康状态
     */
    ModelHealthStatus getModelHealth(String modelName);

    /**
     * 获取所有模型的健康状态
     * 
     * @return 健康状态映射
     */
    Map<String, ModelHealthStatus> getAllModelHealth();

    /**
     * 刷新模型配置
     */
    void refreshModels();

    /**
     * 模型信息
     */
    class ModelInfo {
        private final String name;
        private final String provider;
        private final ModelType type;
        private final String apiKey;
        private final String baseUrl;
        private final Integer maxTokens;
        private final Double temperature;
        private final Map<String, Object> additionalProperties;

        public ModelInfo(String name, String provider, ModelType type, String apiKey, 
                        String baseUrl, Integer maxTokens, Double temperature, 
                        Map<String, Object> additionalProperties) {
            this.name = name;
            this.provider = provider;
            this.type = type;
            this.apiKey = apiKey;
            this.baseUrl = baseUrl;
            this.maxTokens = maxTokens;
            this.temperature = temperature;
            this.additionalProperties = additionalProperties;
        }

        public String getName() {
            return name;
        }

        public String getProvider() {
            return provider;
        }

        public ModelType getType() {
            return type;
        }

        public String getApiKey() {
            return apiKey;
        }

        public String getBaseUrl() {
            return baseUrl;
        }

        public Integer getMaxTokens() {
            return maxTokens;
        }

        public Double getTemperature() {
            return temperature;
        }

        public Map<String, Object> getAdditionalProperties() {
            return additionalProperties;
        }

        @Override
        public String toString() {
            return "ModelInfo{" +
                    "name='" + name + '\'' +
                    ", provider='" + provider + '\'' +
                    ", type=" + type +
                    ", maxTokens=" + maxTokens +
                    ", temperature=" + temperature +
                    '}';
        }
    }

    /**
     * 模型类型枚举
     */
    enum ModelType {
        CHAT, EMBEDDING, IMAGE, AUDIO, MODERATION
    }

    /**
     * 模型健康状态
     */
    class ModelHealthStatus {
        private final String modelName;
        private final boolean healthy;
        private final String status;
        private final String message;
        private final long lastCheckTime;
        private final Map<String, Object> metrics;

        public ModelHealthStatus(String modelName, boolean healthy, String status, 
                               String message, long lastCheckTime, Map<String, Object> metrics) {
            this.modelName = modelName;
            this.healthy = healthy;
            this.status = status;
            this.message = message;
            this.lastCheckTime = lastCheckTime;
            this.metrics = metrics;
        }

        public String getModelName() {
            return modelName;
        }

        public boolean isHealthy() {
            return healthy;
        }

        public String getStatus() {
            return status;
        }

        public String getMessage() {
            return message;
        }

        public long getLastCheckTime() {
            return lastCheckTime;
        }

        public Map<String, Object> getMetrics() {
            return metrics;
        }

        @Override
        public String toString() {
            return "ModelHealthStatus{" +
                    "modelName='" + modelName + '\'' +
                    ", healthy=" + healthy +
                    ", status='" + status + '\'' +
                    ", message='" + message + '\'' +
                    ", lastCheckTime=" + lastCheckTime +
                    '}';
        }
    }
} 