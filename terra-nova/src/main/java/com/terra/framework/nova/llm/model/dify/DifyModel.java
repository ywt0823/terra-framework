package com.terra.framework.nova.llm.model.dify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.AbstractVendorModel;
import com.terra.framework.nova.llm.model.AuthProvider;
import com.terra.framework.nova.llm.model.DefaultAuthProvider;
import com.terra.framework.nova.llm.model.ModelConfig;
import com.terra.framework.nova.llm.model.ModelType;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Dify模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class DifyModel extends AbstractVendorModel {

    /**
     * 参数映射策略
     */
    private final DifyRequestMappingStrategy mappingStrategy;

    /**
     * 构造函数
     *
     * @param config 模型配置
     * @param httpClient HTTP客户端工具
     */
    public DifyModel(ModelConfig config, HttpClientUtils httpClient) {
        super(
            config, 
            httpClient,
            new DifyAdapter(new DifyRequestMappingStrategy(), new DefaultAuthProvider(config.getAuthConfig())),
            new DefaultAuthProvider(config.getAuthConfig())
        );
        this.mappingStrategy = new DifyRequestMappingStrategy();
    }
    
    @Override
    protected String getVendorName() {
        return "Dify";
    }
    
    @Override
    protected ModelType getModelType() {
        return ModelType.DIFY;
    }
    
    @Override
    protected String getModelName() {
        return "Dify App: " + getAppId();
    }
    
    @Override
    protected String getChatEndpoint() {
        return "/chat-messages";
    }
    
    @Override
    protected String getCompletionsEndpoint() {
        return "/completion-messages";
    }
    
    @Override
    protected String buildFullEndpointUrl(String endpoint, Map<String, Object> parameters) {
        String baseUrl = config.getEndpoint();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // 获取应用程序ID
        String appId = mappingStrategy.getAppId(parameters);
        if (appId == null) {
            appId = getAppId();
        }

        // Dify API格式: /api/v1/apps/{app_id}/completion-messages 或 /api/v1/apps/{app_id}/chat-messages
        return baseUrl + "/api/v1/apps/" + appId + endpoint;
    }
    
    @Override
    protected String processStreamData(String chunk) {
        if (chunk.trim().isEmpty()) {
            return null;
        }

        // Dify的流式输出不是标准的SSE格式，需要特殊处理
        if (chunk.startsWith("data: ")) {
            String data = chunk.substring(6).trim();
            if ("[DONE]".equals(data)) {
                return null;
            }

            try {
                JSONObject jsonResponse = JSON.parseObject(data);
                if (jsonResponse.containsKey("event") &&
                    "message".equals(jsonResponse.getString("event"))) {
                    if (jsonResponse.containsKey("answer")) {
                        String content = jsonResponse.getString("answer");
                        if (content != null && !content.isEmpty()) {
                            return content;
                        }
                    }
                }
            } catch (Exception e) {
                log.debug("解析Dify流式数据失败: {}", e.getMessage());
            }
        } else {
            // 尝试作为JSON解析
            try {
                JSONObject jsonResponse = JSON.parseObject(chunk);
                if (jsonResponse.containsKey("answer")) {
                    String content = jsonResponse.getString("answer");
                    if (content != null && !content.isEmpty()) {
                        return content;
                    }
                }
            } catch (Exception e) {
                // 如果不是JSON，则直接返回
                if (!chunk.trim().isEmpty()) {
                    return chunk.trim();
                }
            }
        }
        
        return null;
    }

    /**
     * 获取应用程序ID
     *
     * @return 应用程序ID
     */
    private String getAppId() {
        String modelId = config.getModelId();

        // 如果模型ID包含冒号（如dify:app_id），则提取应用程序ID
        if (modelId.contains(":")) {
            return modelId.substring(modelId.indexOf(':') + 1);
        }

        // 尝试从参数中获取
        Object modelObj = config.getDefaultParameters().get("model");
        if (modelObj != null) {
            String modelStr = modelObj.toString();
            if (modelStr.contains(":")) {
                return modelStr.substring(modelStr.indexOf(':') + 1);
            }
            return modelStr;
        }

        throw new ModelException("无法确定Dify应用程序ID，请在模型ID中使用格式：dify:your_app_id");
    }
}
