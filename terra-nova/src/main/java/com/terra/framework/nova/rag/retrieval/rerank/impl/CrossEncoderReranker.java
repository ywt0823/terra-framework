package com.terra.framework.nova.rag.retrieval.rerank.impl;

import com.terra.framework.nova.llm.model.AIModel;
import com.terra.framework.nova.llm.model.AIModelManager;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.properties.RerankProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * 交叉编码器重排序器
 * 使用交叉编码器模型评估文档与查询的相关性
 * 
 * @author Terra Framework Team
 * @date 2025年6月15日  
 */
@Slf4j
public class CrossEncoderReranker extends AbstractReranker {

    private final AIModelManager modelManager;
    private final RerankProperties properties;
    private final String modelId;
    
    /**
     * 构造函数
     *
     * @param modelManager AI模型管理器
     * @param properties 重排序配置
     */
    public CrossEncoderReranker(AIModelManager modelManager, RerankProperties properties) {
        this.modelManager = modelManager;
        this.properties = properties;
        this.modelId = properties.getModelId().isEmpty() 
                ? "text-similarity" 
                : properties.getModelId();
    }
    
    @Override
    protected double scoreDocument(Document document, String query, Map<String, Object> parameters) {
        try {
            String text = document.getContent();
            AIModel model = modelManager.getModel(modelId);
            
            Map<String, Object> modelParams = new HashMap<>(parameters);
            modelParams.put("temperature", 0.0);
            
            // 构建提示词：查询和文档的组合
            String prompt = query + "\n\n" + text;
            
            // 交叉编码器直接返回相关性分数
            ModelResponse response = model.generate(prompt, modelParams);
            String result = response.getContent().trim();
            
            try {
                return Double.parseDouble(result) / 100.0; // 归一化到0-1
            } catch (NumberFormatException e) {
                log.warn("Failed to parse relevance score: {}", result);
                return 0.5; // 默认中等相关性
            }
        } catch (Exception e) {
            log.error("Error scoring document with CrossEncoder: {}", e.getMessage());
            return 0.0;
        }
    }

    @Override
    public String getName() {
        return "cross-encoder";
    }

    @Override
    public String getDescription() {
        return "交叉编码器重排序器，使用神经网络模型评估文档与查询的相关性";
    }
} 