package com.terra.framework.nova.llm.model.coze;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.model.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * Coze模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class CozeModel extends AbstractVendorModel {

    /**
     * 构造函数
     *
     * @param config     模型配置
     * @param httpClient HTTP客户端工具
     */
    public CozeModel(ModelConfig config, HttpClientUtils httpClient) {
        super(
            config,
            httpClient,
            new CozeAdapter(new CozeRequestMappingStrategy(), new DefaultAuthProvider(config.getAuthConfig())),
            new DefaultAuthProvider(config.getAuthConfig())
        );
    }

    @Override
    protected String getVendorName() {
        return "Coze";
    }

    @Override
    protected ModelType getModelType() {
        return ModelType.COZE;
    }

    @Override
    protected String getModelName() {
        String modelId = config.getModelId();

        // 如果模型ID包含冒号（如coze:bot-id），则提取真实模型名
        if (modelId.contains(":")) {
            return modelId.substring(modelId.indexOf(':') + 1);
        }

        // 尝试从参数中获取
        Object modelObj = config.getDefaultParameters().get("model");
        if (modelObj != null) {
            return modelObj.toString();
        }

        // 默认模型名称
        return "coze-bot";
    }

    @Override
    protected String getChatEndpoint() {
        return "/chat/completions";
    }

    @Override
    protected String getCompletionsEndpoint() {
        return "/chat/completions"; // Coze只有一个统一的端点
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
    protected String processStreamData(String data) {
        try {
            if (data.startsWith("data: ")) {
                data = data.substring(6);
            }

            if ("[DONE]".equals(data)) {
                return null;
            }

            JSONObject jsonResponse = JSON.parseObject(data);
            ModelResponse response = adapter.convertResponse(jsonResponse);
            if (response.getContent() != null && !response.getContent().isEmpty()) {
                return response.getContent();
            }
        } catch (Exception e) {
            log.debug("解析Coze流式数据失败: {}", e.getMessage());
        }

        return null;
    }


}
