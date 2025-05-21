package com.terra.framework.nova.llm.model.deepseek;

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
 * DeepSeek模型实现
 *
 * @author terra-nova
 */
@Slf4j
public class DeepSeekModel extends AbstractVendorModel {

    /**
     * 构造函数
     *
     * @param config     模型配置
     * @param httpClient HTTP客户端工具
     */
    public DeepSeekModel(ModelConfig config, HttpClientUtils httpClient) {
        super(
            config,
            httpClient,
            new DeepSeekAdapter(new DeepSeekRequestMappingStrategy(), new DefaultAuthProvider(config.getAuthConfig())),
            new DefaultAuthProvider(config.getAuthConfig())
        );
    }

    @Override
    protected String getVendorName() {
        return "DeepSeek";
    }

    @Override
    protected ModelType getModelType() {
        return ModelType.DEEPSEEK;
    }

    @Override
    protected String getModelName() {
        String[] parts = config.getModelId().split(":");
        return parts.length > 1 ? parts[1] : "deepseek-chat";
    }

    @Override
    protected String getChatEndpoint() {
        return "/chat/completions";
    }

    @Override
    protected String getCompletionsEndpoint() {
        // DeepSeek API的completions接口只在beta API中可用
        return "/beta/completions";
    }

    @Override
    protected String buildFullEndpointUrl(String endpoint, Map<String, Object> parameters) {
        if (config.getEndpoint() == null || config.getEndpoint().isEmpty()) {
            throw new IllegalStateException("DeepSeek API端点未配置");
        }

        String baseUrl = config.getEndpoint();
        if (baseUrl.endsWith("/")) {
            baseUrl = baseUrl.substring(0, baseUrl.length() - 1);
        }

        // 如果是completions接口，使用beta基础URL
        if (endpoint.equals("/beta/completions")) {
            // 确保使用beta API
            if (baseUrl.contains("/v1")) {
                baseUrl = baseUrl.replace("/v1", "");
            }
            // 如果没有指定beta，则添加/beta
            if (!baseUrl.endsWith("/beta")) {
                return baseUrl + endpoint;
            }
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

                // 从JSON中提取内容
                if (jsonResponse.containsKey("choices")) {
                    JSONObject firstChoice = jsonResponse.getJSONArray("choices").getJSONObject(0);
                    if (firstChoice.containsKey("delta") &&
                        firstChoice.getJSONObject("delta").containsKey("content")) {
                        String content = firstChoice.getJSONObject("delta").getString("content");
                        return content;
                    }
                }
            } catch (Exception e) {
                log.debug("解析DeepSeek流式数据失败: {}", e.getMessage());
            }
        }

        return null;
    }
}
