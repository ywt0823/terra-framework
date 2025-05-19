package com.terra.framework.nova.llm.model.ollama;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.model.AbstractVendorModel;
import com.terra.framework.nova.llm.model.DefaultAuthProvider;
import com.terra.framework.nova.llm.model.ModelConfig;
import com.terra.framework.nova.llm.model.ModelRequest;
import com.terra.framework.nova.llm.model.ModelType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Ollama模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class OllamaModel extends AbstractVendorModel {

    /**
     * 参数映射策略
     */
    private final OllamaRequestMappingStrategy mappingStrategy;

    /**
     * 构造函数
     *
     * @param config 模型配置
     * @param httpClient HTTP客户端工具
     */
    public OllamaModel(ModelConfig config, HttpClientUtils httpClient) {
        super(
            config, 
            httpClient,
            new OllamaAdapter(new OllamaRequestMappingStrategy(), new DefaultAuthProvider(config.getAuthConfig())),
            new DefaultAuthProvider(config.getAuthConfig())
        );
        this.mappingStrategy = new OllamaRequestMappingStrategy();
    }
    
    @Override
    protected String getVendorName() {
        return "Ollama";
    }
    
    @Override
    protected ModelType getModelType() {
        return ModelType.OLLAMA;
    }
    
    @Override
    protected String getModelName() {
        String modelId = config.getModelId();

        // 如果模型ID包含冒号（如ollama:llama2），则提取真实模型名
        if (modelId.contains(":")) {
            return modelId.substring(modelId.indexOf(':') + 1);
        }

        // 尝试从参数中获取
        Object modelObj = config.getDefaultParameters().get("model");
        if (modelObj != null) {
            return modelObj.toString();
        }

        // 默认模型
        return "llama2";
    }
    
    @Override
    protected String getChatEndpoint() {
        return "/api/chat";
    }
    
    @Override
    protected String getCompletionsEndpoint() {
        return "/api/generate";
    }
    
    @Override
    protected String buildFullEndpointUrl(String endpoint, Map<String, Object> parameters) {
        String baseUrl = config.getEndpoint();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // Ollama有时需要动态确定端点
        JSONObject request = new JSONObject(parameters);
        String actualEndpoint = determineEndpoint(request);

        if (!actualEndpoint.startsWith("/")) {
            actualEndpoint = "/" + actualEndpoint;
        }

        return baseUrl + actualEndpoint;
    }
    
    @Override
    protected String processStreamData(String chunk) {
        if (chunk.trim().isEmpty()) {
            return null;
        }

        try {
            // 解析JSON
            JSONObject jsonResponse = JSON.parseObject(chunk);

            // 从JSON中提取内容
            String content = null;

            if (jsonResponse.containsKey("response")) {
                // generate API的流式响应
                content = jsonResponse.getString("response");
            } else if (jsonResponse.containsKey("message")) {
                // chat API的流式响应
                JSONObject message = jsonResponse.getJSONObject("message");
                content = message.getString("content");
            }

            if (content != null && !content.isEmpty()) {
                return content;
            }
        } catch (Exception e) {
            log.debug("解析Ollama流式数据失败: {}", e.getMessage());
        }
        
        return null;
    }
    
    /**
     * 基于请求内容确定使用哪个API端点
     *
     * @param request 请求对象
     * @return API端点路径
     */
    private String determineEndpoint(JSONObject request) {
        // 判断是否是聊天请求
        boolean isChat = request.containsKey("messages") && request.getJSONArray("messages").size() > 0;
        
        if (isChat) {
            return getChatEndpoint();
        } else {
            return getCompletionsEndpoint();
        }
    }
}
