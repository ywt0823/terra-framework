package com.terra.framework.nova.manager.impl;

import com.terra.framework.nova.manager.TerraModelManager;
import com.terra.framework.nova.properties.TerraNovaProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 默认的 Terra 模型管理器实现
 * 
 * <p>提供基于配置的模型管理功能，包括：
 * <ul>
 *   <li>从配置文件加载模型信息</li>
 *   <li>模型注册和注销</li>
 *   <li>模型健康状态监控</li>
 *   <li>模型配置动态刷新</li>
 * </ul>
 * 
 * @author terra-nova
 * @since 0.0.1
 */
public class DefaultTerraModelManager implements TerraModelManager {

    private static final Logger logger = LoggerFactory.getLogger(DefaultTerraModelManager.class);

    private final TerraNovaProperties properties;
    private final Map<String, ModelInfo> modelRegistry = new ConcurrentHashMap<>();
    private final Map<String, ModelHealthStatus> healthStatusCache = new ConcurrentHashMap<>();

    public DefaultTerraModelManager(TerraNovaProperties properties) {
        this.properties = properties;
        initializeModels();
        logger.info("Default Terra Model Manager initialized with {} models", modelRegistry.size());
    }

    @Override
    public String getDefaultModelName() {
        String defaultProvider = properties.getModels().getDefaultProvider();
        if (defaultProvider == null) {
            return null;
        }

        // 查找默认提供商的第一个模型
        return modelRegistry.values().stream()
                .filter(model -> defaultProvider.equals(model.getProvider()))
                .map(ModelInfo::getName)
                .findFirst()
                .orElse(null);
    }

    @Override
    public List<String> getAvailableModels() {
        return new ArrayList<>(modelRegistry.keySet());
    }

    @Override
    public boolean isModelAvailable(String modelName) {
        return modelRegistry.containsKey(modelName);
    }

    @Override
    public Optional<ModelInfo> getModelInfo(String modelName) {
        return Optional.ofNullable(modelRegistry.get(modelName));
    }

    @Override
    public Map<String, ModelInfo> getAllModelInfo() {
        return new HashMap<>(modelRegistry);
    }

    @Override
    public void registerModel(String modelName, ModelInfo modelInfo) {
        if (modelName == null || modelInfo == null) {
            throw new IllegalArgumentException("Model name and info cannot be null");
        }

        modelRegistry.put(modelName, modelInfo);
        
        // 初始化健康状态
        ModelHealthStatus healthStatus = new ModelHealthStatus(
                modelName, 
                true, 
                "REGISTERED", 
                "Model registered successfully", 
                System.currentTimeMillis(),
                new HashMap<>()
        );
        healthStatusCache.put(modelName, healthStatus);
        
        logger.info("Model registered: {}", modelName);
    }

    @Override
    public void unregisterModel(String modelName) {
        if (modelName == null) {
            return;
        }

        ModelInfo removed = modelRegistry.remove(modelName);
        healthStatusCache.remove(modelName);
        
        if (removed != null) {
            logger.info("Model unregistered: {}", modelName);
        }
    }

    @Override
    public ModelHealthStatus getModelHealth(String modelName) {
        ModelHealthStatus cached = healthStatusCache.get(modelName);
        if (cached != null) {
            return cached;
        }

        // 如果没有缓存的健康状态，创建一个默认的
        if (isModelAvailable(modelName)) {
            ModelHealthStatus defaultStatus = new ModelHealthStatus(
                    modelName,
                    true,
                    "UNKNOWN",
                    "Health status not checked yet",
                    System.currentTimeMillis(),
                    new HashMap<>()
            );
            healthStatusCache.put(modelName, defaultStatus);
            return defaultStatus;
        }

        return new ModelHealthStatus(
                modelName,
                false,
                "NOT_FOUND",
                "Model not found",
                System.currentTimeMillis(),
                new HashMap<>()
        );
    }

    @Override
    public Map<String, ModelHealthStatus> getAllModelHealth() {
        Map<String, ModelHealthStatus> allHealth = new HashMap<>();
        
        // 确保所有注册的模型都有健康状态
        for (String modelName : modelRegistry.keySet()) {
            allHealth.put(modelName, getModelHealth(modelName));
        }
        
        return allHealth;
    }

    @Override
    public void refreshModels() {
        logger.info("Refreshing model configuration...");
        
        // 清除现有模型（保留手动注册的模型）
        Set<String> configuredModels = new HashSet<>();
        
        // 重新加载配置中的模型
        loadModelsFromConfiguration(configuredModels);
        
        // 移除不再配置的模型
        modelRegistry.entrySet().removeIf(entry -> {
            String modelName = entry.getKey();
            if (!configuredModels.contains(modelName)) {
                logger.info("Removing model no longer in configuration: {}", modelName);
                healthStatusCache.remove(modelName);
                return true;
            }
            return false;
        });
        
        logger.info("Model configuration refreshed. Total models: {}", modelRegistry.size());
    }

    /**
     * 初始化模型
     */
    private void initializeModels() {
        Set<String> configuredModels = new HashSet<>();
        loadModelsFromConfiguration(configuredModels);
    }

    /**
     * 从配置加载模型
     * 
     * @param configuredModels 用于跟踪配置的模型名称
     */
    private void loadModelsFromConfiguration(Set<String> configuredModels) {
        Map<String, TerraNovaProperties.Models.Provider> providers = properties.getModels().getProviders();
        
        for (Map.Entry<String, TerraNovaProperties.Models.Provider> providerEntry : providers.entrySet()) {
            String providerName = providerEntry.getKey();
            TerraNovaProperties.Models.Provider provider = providerEntry.getValue();
            
            if (provider.getModels() != null) {
                for (TerraNovaProperties.Models.Model modelConfig : provider.getModels()) {
                    String modelName = modelConfig.getName();
                    if (modelName != null) {
                        ModelInfo modelInfo = createModelInfo(providerName, provider, modelConfig);
                        registerModel(modelName, modelInfo);
                        configuredModels.add(modelName);
                        
                        logger.debug("Loaded model from configuration: {} (provider: {})", 
                                modelName, providerName);
                    }
                }
            }
        }
    }

    /**
     * 创建模型信息对象
     * 
     * @param providerName 提供商名称
     * @param provider 提供商配置
     * @param modelConfig 模型配置
     * @return 模型信息
     */
    private ModelInfo createModelInfo(String providerName, 
                                    TerraNovaProperties.Models.Provider provider,
                                    TerraNovaProperties.Models.Model modelConfig) {
        
        // 转换模型类型
        ModelType modelType = convertModelType(modelConfig.getType());
        
        // 创建附加属性
        Map<String, Object> additionalProperties = new HashMap<>();
        additionalProperties.put("provider", providerName);
        additionalProperties.put("configuredAt", System.currentTimeMillis());
        
        return new ModelInfo(
                modelConfig.getName(),
                providerName,
                modelType,
                provider.getApiKey(),
                provider.getBaseUrl(),
                modelConfig.getMaxTokens(),
                modelConfig.getTemperature(),
                additionalProperties
        );
    }

    /**
     * 转换模型类型
     * 
     * @param configType 配置中的模型类型
     * @return 内部模型类型
     */
    private ModelType convertModelType(TerraNovaProperties.Models.ModelType configType) {
        if (configType == null) {
            return ModelType.CHAT;
        }
        
        switch (configType) {
            case CHAT:
                return ModelType.CHAT;
            case EMBEDDING:
                return ModelType.EMBEDDING;
            case IMAGE:
                return ModelType.IMAGE;
            case AUDIO:
                return ModelType.AUDIO;
            case MODERATION:
                return ModelType.MODERATION;
            default:
                return ModelType.CHAT;
        }
    }

    /**
     * 更新模型健康状态
     * 
     * @param modelName 模型名称
     * @param healthy 是否健康
     * @param status 状态
     * @param message 消息
     * @param metrics 指标
     */
    public void updateModelHealth(String modelName, boolean healthy, String status, 
                                String message, Map<String, Object> metrics) {
        ModelHealthStatus healthStatus = new ModelHealthStatus(
                modelName,
                healthy,
                status,
                message,
                System.currentTimeMillis(),
                metrics != null ? new HashMap<>(metrics) : new HashMap<>()
        );
        
        healthStatusCache.put(modelName, healthStatus);
        logger.debug("Updated health status for model {}: {}", modelName, status);
    }

    /**
     * 获取指定提供商的模型
     * 
     * @param providerName 提供商名称
     * @return 模型列表
     */
    public List<String> getModelsByProvider(String providerName) {
        return modelRegistry.values().stream()
                .filter(model -> providerName.equals(model.getProvider()))
                .map(ModelInfo::getName)
                .collect(Collectors.toList());
    }

    /**
     * 获取指定类型的模型
     * 
     * @param modelType 模型类型
     * @return 模型列表
     */
    public List<String> getModelsByType(ModelType modelType) {
        return modelRegistry.values().stream()
                .filter(model -> modelType.equals(model.getType()))
                .map(ModelInfo::getName)
                .collect(Collectors.toList());
    }

    /**
     * 获取配置属性（用于测试）
     * 
     * @return 配置属性
     */
    public TerraNovaProperties getProperties() {
        return properties;
    }
} 