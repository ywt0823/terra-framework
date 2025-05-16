package com.terra.framework.nova.llm.manager;

import com.terra.framework.nova.llm.core.LLMModel;
import com.terra.framework.nova.llm.core.ModelConfig;
import com.terra.framework.nova.llm.core.ModelFactory;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 模型管理器
 */
@Slf4j
public class ModelManager {

    private final Map<String, LLMModel> modelCache = new ConcurrentHashMap<>();
    private final Map<String, ModelConfig> configCache = new ConcurrentHashMap<>();

    /**
     * 获取模型实例
     *
     * @param modelId 模型ID
     * @return LLM模型实例
     */
    public LLMModel getModel(String modelId) {
        return modelCache.computeIfAbsent(modelId, this::createModel);
    }

    /**
     * 注册模型配置
     *
     * @param modelId 模型ID
     * @param config 模型配置
     */
    public void registerConfig(String modelId, ModelConfig config) {
        configCache.put(modelId, config);
    }

    /**
     * 创建模型实例
     *
     * @param modelId 模型ID
     * @return LLM模型实例
     */
    private LLMModel createModel(String modelId) {
        ModelConfig config = loadConfig(modelId);
        LLMModel model = ModelFactory.createModel(config.getType(), config);
        model.init();
        return model;
    }

    /**
     * 加载模型配置
     *
     * @param modelId 模型ID
     * @return 模型配置
     */
    private ModelConfig loadConfig(String modelId) {
        ModelConfig config = configCache.get(modelId);
        if (config == null) {
            throw new IllegalStateException("Model config not found for modelId: " + modelId);
        }
        return config;
    }

    /**
     * 关闭所有模型
     */
    public void shutdown() {
        log.info("Shutting down all models");
        modelCache.values().forEach(LLMModel::close);
        modelCache.clear();
    }
} 