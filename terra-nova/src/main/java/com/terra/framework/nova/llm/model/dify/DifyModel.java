package com.terra.framework.nova.llm.model.dify;

import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.common.JsonUtils;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.core.ModelConfig;
import com.terra.framework.nova.llm.model.base.LLMModel;
import com.terra.framework.nova.llm.util.ModelResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

/**
 * Dify.AI模型适配器
 */
@Slf4j
public class DifyModel implements LLMModel {

    private final ModelConfig config;
    private final HttpClientUtils httpClientUtils;
    private static final String DEFAULT_ENDPOINT = "https://api.dify.ai/v1";
    private String chatCompletionUrl;
    private String messageCompletionUrl;
    private String streamChatCompletionUrl;
    private Header[] headers;
    private String appId;
    private boolean useKnowledgeBase;
    private String knowledgeBaseId;

    public DifyModel(ModelConfig config, HttpClientUtils httpClientUtils) {
        this.config = config;
        this.httpClientUtils = httpClientUtils;
    }

    @Override
    public void init() {
        log.info("Initializing Dify model with endpoint: {}", config.getApiEndpoint());

        // 获取配置信息
        String baseUrl = config.getApiEndpoint() != null ? config.getApiEndpoint() : DEFAULT_ENDPOINT;
        appId = (String) config.getExtraParams().getOrDefault("appId", "");
        if (appId.isEmpty()) {
            throw new IllegalArgumentException("Dify appId不能为空");
        }

        // 初始化API端点
        chatCompletionUrl = baseUrl + "/chat-messages";
        messageCompletionUrl = baseUrl + "/messages";
        streamChatCompletionUrl = baseUrl + "/chat-messages/stream";

        // 设置请求头
        headers[0] = new BasicHeader("Authorization", "Bearer " + config.getApiKey());
        headers[1] = new BasicHeader("Content-Type", "application/json");

        // 知识库相关配置
        useKnowledgeBase = Boolean.parseBoolean(config.getExtraParams().getOrDefault("useKnowledgeBase", "false").toString());
        if (useKnowledgeBase) {
            knowledgeBaseId = (String) config.getExtraParams().getOrDefault("knowledgeBaseId", "");
            if (knowledgeBaseId.isEmpty()) {
                log.warn("启用了知识库功能但未指定knowledgeBaseId，将使用默认知识库");
            }
        }

        log.info("Dify model initialized with appId: {}, useKnowledgeBase: {}", appId, useKnowledgeBase);
    }

    @Override
    public String predict(String prompt) {
        try {
            log.debug("Sending prediction request to Dify with prompt: {}", prompt);

            // 构建请求参数
            Map<String, Object> requestBody = buildRequestBody(prompt);

            // 发送请求
            JSONObject result = httpClientUtils.sendPostDataByJson(chatCompletionUrl, JsonUtils.objectCovertToJson(requestBody), StandardCharsets.UTF_8, headers);

            return extractContent(result);
        } catch (Exception e) {
            log.error("Error during Dify prediction", e);
            throw new RuntimeException("Dify prediction failed", e);
        }
    }

    @Override
    public Publisher<String> predictStream(String prompt) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> predict(prompt));
        return ModelResponseHandler.createStreamPublisher(future);
    }

    private Map<String, Object> buildRequestBody(String prompt) {
        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("inputs", new HashMap<>());
        requestBody.put("query", prompt);
        requestBody.put("response_mode", "blocking");
        requestBody.put("conversation_id", "");
        requestBody.put("user", "user");

        // 添加模型控制参数
        Map<String, Object> modelParams = new HashMap<>();
        modelParams.put("temperature", config.getTemperature());
        modelParams.put("max_tokens", config.getMaxTokens());
        requestBody.put("model_parameters", modelParams);

        // 如果使用知识库，添加相关配置
        if (useKnowledgeBase) {
            Map<String, Object> retrievalParams = new HashMap<>();
            retrievalParams.put("enabled", true);
            if (!knowledgeBaseId.isEmpty()) {
                retrievalParams.put("knowledge_id", knowledgeBaseId);
            }
            requestBody.put("retrieval", retrievalParams);
        }

        return requestBody;
    }

    private String extractContent(Map<String, Object> response) {
        try {
            // 检查是否有错误
            if (response.containsKey("error")) {
                Map<String, Object> error = (Map<String, Object>) response.get("error");
                String errorMsg = (String) error.getOrDefault("message", "Unknown error");
                throw new RuntimeException("Dify API error: " + errorMsg);
            }

            // 提取答案
            if (response.containsKey("answer")) {
                return (String) response.get("answer");
            }

            // 如果答案不在顶层，尝试从data中提取
            if (response.containsKey("data")) {
                Map<String, Object> data = (Map<String, Object>) response.get("data");
                if (data.containsKey("answer")) {
                    return (String) data.get("answer");
                }

                // 尝试从message中提取
                if (data.containsKey("message")) {
                    Map<String, Object> message = (Map<String, Object>) data.get("message");
                    if (message.containsKey("content")) {
                        return (String) message.get("content");
                    }
                }
            }

            throw new RuntimeException("无法从Dify响应中提取内容");

        } catch (Exception e) {
            throw new RuntimeException("解析Dify响应失败: " + e.getMessage(), e);
        }
    }

    @Override
    public void close() {
        log.info("Closing Dify model");
        // HttpClientUtils是共享实例，不需要关闭
    }
}
