package com.terra.framework.nova.llm.model.claude;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.model.AbstractVendorModel;
import com.terra.framework.nova.llm.model.DefaultAuthProvider;
import com.terra.framework.nova.llm.model.ModelConfig;
import com.terra.framework.nova.llm.model.ModelType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Claude模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class ClaudeModel extends AbstractVendorModel {

    /**
     * 构造函数
     *
     * @param config 模型配置
     * @param httpClient HTTP客户端工具
     */
    public ClaudeModel(ModelConfig config, HttpClientUtils httpClient) {
        super(
            config, 
            httpClient,
            new ClaudeAdapter(new ClaudeRequestMappingStrategy(), new DefaultAuthProvider(config.getAuthConfig())),
            new DefaultAuthProvider(config.getAuthConfig())
        );
    }
    
    @Override
    protected String getVendorName() {
        return "Anthropic";
    }
    
    @Override
    protected ModelType getModelType() {
        return ModelType.CLAUDE;
    }
    
    @Override
    protected String getModelName() {
        String modelId = config.getModelId();

        // 如果模型ID包含冒号（如claude:claude-3-opus-20240229），则提取真实模型名
        if (modelId.contains(":")) {
            return modelId.substring(modelId.indexOf(':') + 1);
        }

        // 尝试从参数中获取
        Object modelObj = config.getDefaultParameters().get("model");
        if (modelObj != null) {
            return modelObj.toString();
        }

        // 默认模型名称
        return "claude-3-opus-20240229";
    }
    
    @Override
    protected String getChatEndpoint() {
        return "/v1/messages";
    }
    
    @Override
    protected String getCompletionsEndpoint() {
        return "/v1/messages"; // Claude只使用一个统一的端点
    }
    
    @Override
    protected String buildFullEndpointUrl(String endpoint, Map<String, Object> parameters) {
        String baseUrl = config.getEndpoint();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        if (!endpoint.startsWith("/")) {
            endpoint = "/" + endpoint;
        }

        return baseUrl + endpoint;
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

                // 从JSON中提取内容
                if (jsonResponse.containsKey("type") &&
                    "content_block_delta".equals(jsonResponse.getString("type"))) {
                    JSONObject delta = jsonResponse.getJSONObject("delta");
                    if (delta.containsKey("text")) {
                        String content = delta.getString("text");
                        if (content != null && !content.isEmpty()) {
                            return content;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("解析Claude流式数据失败: {}", e.getMessage());
            }
        }
        
        return null;
    }
}
