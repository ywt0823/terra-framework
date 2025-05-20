package com.terra.framework.nova.rag.retrieval.rerank.impl;

import com.terra.framework.nova.llm.model.AIModel;
import com.terra.framework.nova.llm.model.AIModelManager;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.properties.RerankProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * LLM重排序器
 * 使用大语言模型评估文档与查询的相关性
 * 
 * @author Terra Framework Team
 * @date 2025年6月15日  
 */
@Slf4j
public class LLMReranker extends AbstractReranker {

    private final AIModelManager modelManager;
    private final RerankProperties properties;
    private final String modelId;
    private final Pattern scorePattern = Pattern.compile("\\b([0-9]|[1-9][0-9]|100)\\b");
    
    /**
     * 构造函数
     *
     * @param modelManager AI模型管理器
     * @param properties 重排序配置
     */
    public LLMReranker(AIModelManager modelManager, RerankProperties properties) {
        this.modelManager = modelManager;
        this.properties = properties;
        this.modelId = properties.getModelId().isEmpty() 
                ? modelManager.getModel("deepseek:deepseek-chat").getModelInfo().getModelId() 
                : properties.getModelId();
    }
    
    @Override
    protected double scoreDocument(Document document, String query, Map<String, Object> parameters) {
        try {
            String text = document.getContent();
            AIModel model = modelManager.getModel(modelId);
            
            // 合并参数
            Map<String, Object> modelParams = new HashMap<>();
            modelParams.put("temperature", properties.getLlmRerank().getTemperature());
            if (parameters != null) {
                modelParams.putAll(parameters);
            }
            if (properties.getLlmRerank().getParameters() != null) {
                modelParams.putAll(properties.getLlmRerank().getParameters());
            }
            
            // 构建提示词，替换{query}和{content}占位符
            String promptTemplate = properties.getLlmRerank().getPromptTemplate();
            String prompt = promptTemplate
                    .replace("{query}", query)
                    .replace("{content}", text);
            
            // 获取LLM响应
            ModelResponse response = model.generate(prompt, modelParams);
            String result = response.getContent().trim();
            
            // 提取分数
            return extractScore(result);
        } catch (Exception e) {
            log.error("Error scoring document with LLM: {}", e.getMessage());
            return 0.0;
        }
    }
    
    /**
     * 从LLM响应中提取相关性分数
     *
     * @param response LLM响应文本
     * @return 提取的分数（0-1范围）
     */
    private double extractScore(String response) {
        try {
            Matcher matcher = scorePattern.matcher(response);
            if (matcher.find()) {
                int score = Integer.parseInt(matcher.group(1));
                return score / 100.0; // 归一化到0-1
            }
            log.warn("Failed to extract score from LLM response: {}", response);
            return 0.5; // 默认中等相关性
        } catch (Exception e) {
            log.error("Error extracting score from LLM response: {}", e.getMessage());
            return 0.5;
        }
    }

    @Override
    public String getName() {
        return "llm";
    }

    @Override
    public String getDescription() {
        return "基于大语言模型的重排序器，使用LLM评估文档与查询的相关性";
    }
} 