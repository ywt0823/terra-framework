package com.terra.framework.nova.llm.model.openai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * OpenAI模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class OpenAIModel extends AbstractVendorModel {

    /**
     * 构造函数
     *
     * @param config 模型配置
     * @param httpClient HTTP客户端工具
     */
    public OpenAIModel(ModelConfig config, HttpClientUtils httpClient) {
        super(
            config, 
            httpClient,
            new OpenAIAdapter(new OpenAIRequestMappingStrategy(), new DefaultAuthProvider(config.getAuthConfig())),
            new DefaultAuthProvider(config.getAuthConfig())
        );
    }
    
    @Override
    protected String getVendorName() {
        return "OpenAI";
    }
    
    @Override
    protected ModelType getModelType() {
        return ModelType.OPENAI;
    }
    
    @Override
    protected String getModelName() {
        String[] parts = config.getModelId().split(":");
        return parts.length > 1 ? parts[1] : "gpt-3.5-turbo";
    }
    
    @Override
    protected String getChatEndpoint() {
        return "/v1/chat/completions";
    }
    
    @Override
    protected String getCompletionsEndpoint() {
        return "/v1/completions";
    }
    
    @Override
    protected String buildFullEndpointUrl(String endpoint, Map<String, Object> parameters) {
        if (config.getEndpoint() == null || config.getEndpoint().isEmpty()) {
            throw new IllegalStateException("OpenAI API端点未配置");
        }
        
        String baseUrl = config.getEndpoint();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }
        
        if (endpoint.startsWith("/")) {
            return baseUrl + endpoint;
        } else {
            return baseUrl + "/" + endpoint;
        }
    }
    
    @Override
    protected String processStreamData(String chunk) {
        if (chunk.startsWith("data: ")) {
            String data = chunk.substring(6).trim();
            
            // 处理特殊情况
            if ("[DONE]".equals(data)) {
                return null;
            }
            
            try {
                // 解析JSON
                JSONObject jsonResponse = JSON.parseObject(data);
                
                String content = extractContentFromStreamingResponse(jsonResponse);
                if (content != null && !content.isEmpty()) {
                    return content;
                }
            } catch (Exception e) {
                log.debug("解析OpenAI流式数据失败: {}", e.getMessage());
            }
        }
        
        return null;
    }
    
    /**
     * 从流式响应中提取内容
     *
     * @param jsonResponse JSON响应
     * @return 内容
     */
    private String extractContentFromStreamingResponse(JSONObject jsonResponse) {
        if (jsonResponse.containsKey("choices")) {
            JSONObject firstChoice = jsonResponse.getJSONArray("choices").getJSONObject(0);
            
            if (firstChoice.containsKey("delta")) {
                JSONObject delta = firstChoice.getJSONObject("delta");
                if (delta.containsKey("content")) {
                    return delta.getString("content");
                }
            }
        }
        
        return null;
    }
}
