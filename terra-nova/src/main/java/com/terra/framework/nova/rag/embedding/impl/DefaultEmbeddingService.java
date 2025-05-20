package com.terra.framework.nova.rag.embedding.impl;

import com.terra.framework.nova.llm.model.AIModel;
import com.terra.framework.nova.llm.model.AIModelManager;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.rag.embedding.EmbeddingModel;
import com.terra.framework.nova.rag.embedding.EmbeddingService;
import com.terra.framework.nova.rag.exception.EmbeddingException;
import com.terra.framework.nova.rag.properties.EmbeddingProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认嵌入服务实现
 * 基于Nova LLM模型能力
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Slf4j
public class DefaultEmbeddingService implements EmbeddingService {

    private final AIModelManager modelManager;
    private final EmbeddingProperties properties;
    private final Map<String, EmbeddingModel> embeddingModelCache = new ConcurrentHashMap<>();
    
    /**
     * 创建嵌入服务
     *
     * @param modelManager LLM模型管理器
     * @param properties 嵌入配置
     */
    public DefaultEmbeddingService(AIModelManager modelManager, EmbeddingProperties properties) {
        this.modelManager = modelManager;
        this.properties = properties;
        
        // 预加载默认模型
        getEmbeddingModel();
    }
    
    @Override
    public float[] createEmbedding(String text) {
        return createEmbedding(text, properties.getModelId());
    }
    
    @Override
    public List<float[]> createEmbeddings(List<String> texts) {
        return createEmbeddings(texts, properties.getModelId());
    }
    
    @Override
    public float[] createEmbedding(String text, String modelId) {
        if (text == null || text.trim().isEmpty()) {
            return new float[getEmbeddingModel(modelId).getDimension()];
        }
        
        try {
            EmbeddingModel embeddingModel = getEmbeddingModel(modelId);
            return embeddingModel.embed(text);
        } catch (Exception e) {
            log.error("创建嵌入向量失败", e);
            throw new EmbeddingException("创建嵌入向量失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public List<float[]> createEmbeddings(List<String> texts, String modelId) {
        if (texts == null || texts.isEmpty()) {
            return Collections.emptyList();
        }
        
        try {
            // 分批处理，避免请求过大
            int batchSize = properties.getBatchSize();
            List<float[]> allEmbeddings = new ArrayList<>(texts.size());
            
            for (int i = 0; i < texts.size(); i += batchSize) {
                int end = Math.min(i + batchSize, texts.size());
                List<String> batch = texts.subList(i, end);
                
                EmbeddingModel embeddingModel = getEmbeddingModel(modelId);
                List<float[]> batchEmbeddings = embeddingModel.embed(batch);
                
                allEmbeddings.addAll(batchEmbeddings);
            }
            
            return allEmbeddings;
        } catch (Exception e) {
            log.error("批量创建嵌入向量失败", e);
            throw new EmbeddingException("批量创建嵌入向量失败: " + e.getMessage(), e);
        }
    }
    
    @Override
    public EmbeddingModel getEmbeddingModel() {
        return getEmbeddingModel(properties.getModelId());
    }
    
    @Override
    public EmbeddingModel getEmbeddingModel(String modelId) {
        if (modelId == null || modelId.trim().isEmpty()) {
            modelId = properties.getModelId();
        }
        
        return embeddingModelCache.computeIfAbsent(modelId, this::createEmbeddingModel);
    }
    
    @Override
    public Map<String, EmbeddingModel> getAvailableModels() {
        return Collections.unmodifiableMap(embeddingModelCache);
    }
    
    /**
     * 创建嵌入模型
     *
     * @param modelId 模型ID
     * @return 嵌入模型
     */
    private EmbeddingModel createEmbeddingModel(String modelId) {
        try {
            AIModel aiModel = modelManager.getModel(modelId);
            
            if (aiModel == null) {
                throw new EmbeddingException("模型未找到: " + modelId);
            }
            
            return new DefaultEmbeddingModel(aiModel, properties.getDimension());
        } catch (Exception e) {
            log.error("创建嵌入模型失败: {}", modelId, e);
            throw new EmbeddingException("创建嵌入模型失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 默认嵌入模型实现
     * 基于Nova LLM模型
     */
    private static class DefaultEmbeddingModel implements EmbeddingModel {
        
        private final AIModel aiModel;
        private final int dimension;
        
        public DefaultEmbeddingModel(AIModel aiModel, int dimension) {
            this.aiModel = aiModel;
            this.dimension = dimension;
        }
        
        @Override
        public String getModelId() {
            return aiModel.getModelInfo().getModelId();
        }
        
        @Override
        public int getDimension() {
            return dimension;
        }
        
        @Override
        public float[] embed(String text) {
            Map<String, Object> params = new HashMap<>();
            params.put("input", text);
            params.put("encoding_format", "float");
            
            ModelResponse response = aiModel.generate(text, params);
            
            // 解析嵌入向量
            return parseEmbeddingFromResponse(response);
        }
        
        @Override
        public List<float[]> embed(List<String> texts) {
            List<float[]> results = new ArrayList<>(texts.size());
            
            for (String text : texts) {
                results.add(embed(text));
            }
            
            return results;
        }
        
        /**
         * 从模型响应中解析嵌入向量
         *
         * @param response 模型响应
         * @return 嵌入向量
         */
        private float[] parseEmbeddingFromResponse(ModelResponse response) {
            try {
                // 根据不同模型，可能需要不同的解析逻辑
                Object rawResponse = response.getRawResponse();
                if (rawResponse instanceof Map) {
                    @SuppressWarnings("unchecked")
                    Map<String, Object> map = (Map<String, Object>) rawResponse;
                    
                    if (map.containsKey("data")) {
                        @SuppressWarnings("unchecked")
                        List<Map<String, Object>> data = (List<Map<String, Object>>) map.get("data");
                        
                        if (!data.isEmpty() && data.get(0).containsKey("embedding")) {
                            @SuppressWarnings("unchecked")
                            List<Number> embedding = (List<Number>) data.get(0).get("embedding");
                            
                            float[] result = new float[embedding.size()];
                            for (int i = 0; i < embedding.size(); i++) {
                                result[i] = embedding.get(i).floatValue();
                            }
                            
                            return result;
                        }
                    }
                    
                    // 尝试直接获取embedding字段
                    if (map.containsKey("embedding")) {
                        @SuppressWarnings("unchecked")
                        List<Number> embedding = (List<Number>) map.get("embedding");
                        
                        float[] result = new float[embedding.size()];
                        for (int i = 0; i < embedding.size(); i++) {
                            result[i] = embedding.get(i).floatValue();
                        }
                        
                        return result;
                    }
                }
                
                // 如果无法解析，尝试直接从内容解析
                String content = response.getContent();
                if (content != null && !content.isEmpty()) {
                    // 尝试解析JSON字符串中的数组
                    if (content.startsWith("[") && content.endsWith("]")) {
                        String[] values = content.substring(1, content.length() - 1).split(",");
                        float[] result = new float[values.length];
                        
                        for (int i = 0; i < values.length; i++) {
                            result[i] = Float.parseFloat(values[i].trim());
                        }
                        
                        return result;
                    }
                }
                
                // 如果以上方法都无法解析，返回空向量
                log.warn("无法解析嵌入向量，返回空向量");
                return new float[dimension];
            } catch (Exception e) {
                log.error("解析嵌入向量失败", e);
                return new float[dimension];
            }
        }
    }
} 