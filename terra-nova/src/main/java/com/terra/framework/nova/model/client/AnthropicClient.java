package com.terra.framework.nova.model.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * Anthropic Claude模型客户端实现
 *
 * @author terra-nova
 */
@Slf4j
public class AnthropicClient implements ModelClient {

    private static final String API_URL = "https://api.anthropic.com/v1/messages";
    private static final String ANTHROPIC_VERSION = "2023-06-01";

    private final HttpClientUtils httpClientUtils;
    private final String apiKey;
    private final String defaultModel;
    private final String[] supportedModels;
    private volatile ClientStatus clientStatus = ClientStatus.INITIALIZING;

    public AnthropicClient(HttpClientUtils httpClientUtils, String apiKey, String defaultModel) {
        this.httpClientUtils = httpClientUtils;
        this.apiKey = apiKey;
        this.defaultModel = defaultModel;
        this.supportedModels = new String[]{
                "claude-3-opus-20240229",
                "claude-3-sonnet-20240229",
                "claude-3-haiku-20240307",
                "claude-2.1",
                "claude-2.0",
                "claude-instant-1.2"
        };
        init();
    }

    @Override
    public String generate(String prompt, Map<String, Object> parameters) {
        try {
            clientStatus = ClientStatus.BUSY;

            Map<String, Object> requestParams = buildRequestParams(prompt, parameters);
            String requestBody = JSONObject.toJSONString(requestParams);

            Header contentTypeHeader = new BasicHeader("Content-Type", "application/json");
            Header authHeader = new BasicHeader("x-api-key", apiKey);
            Header versionHeader = new BasicHeader("anthropic-version", ANTHROPIC_VERSION);

            JSONObject response = httpClientUtils.sendPostDataByJson(
                    API_URL,
                    requestBody,
                    StandardCharsets.UTF_8,
                    contentTypeHeader, authHeader, versionHeader
            );

            // 解析响应
            if (response != null && response.containsKey("content")) {
                JSONArray contentArray = response.getJSONArray("content");
                if (!contentArray.isEmpty()) {
                    StringBuilder result = new StringBuilder();
                    for (int i = 0; i < contentArray.size(); i++) {
                        JSONObject content = contentArray.getJSONObject(i);
                        if ("text".equals(content.getString("type"))) {
                            result.append(content.getString("text"));
                        }
                    }
                    return result.toString();
                }
            }

            log.error("Anthropic响应格式错误: {}", response);
            clientStatus = ClientStatus.ERROR;
            return null;
        } catch (Exception e) {
            log.error("调用Anthropic API失败", e);
            clientStatus = ClientStatus.ERROR;
            return null;
        } finally {
            clientStatus = ClientStatus.READY;
        }
    }

    @Override
    public CompletableFuture<String> generateAsync(String prompt, Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> generate(prompt, parameters), Executors.newSingleThreadExecutor());
    }

    @Override
    public void generateStream(String prompt, Map<String, Object> parameters, StreamResponseHandler handler) {
        // 流式API实现，简化处理
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> requestParams = buildRequestParams(prompt, parameters);
                requestParams.put("stream", true);

                // 这里应该实现真正的流式处理，暂时简化处理
                String response = generate(prompt, parameters);
                if (response != null) {
                    handler.onChunk(response);
                    handler.onComplete();
                } else {
                    handler.onError(new RuntimeException("生成响应失败"));
                }
            } catch (Exception e) {
                handler.onError(e);
            }
        });
    }

    @Override
    public String[] getSupportedModels() {
        return supportedModels;
    }

    @Override
    public ClientStatus getStatus() {
        return clientStatus;
    }

    @Override
    public void init() {
        log.info("初始化Anthropic客户端");

        try {
            // 简单测试以验证连接
            Header authHeader = new BasicHeader("x-api-key", apiKey);
            Header versionHeader = new BasicHeader("anthropic-version", ANTHROPIC_VERSION);
            
            // 在实际环境中应使用合适的API端点验证连接
            // 此处使用简单方法标记为已初始化
            log.info("Anthropic客户端初始化成功");
            clientStatus = ClientStatus.READY;
        } catch (Exception e) {
            log.error("Anthropic客户端初始化失败", e);
            clientStatus = ClientStatus.ERROR;
        }
    }

    @Override
    public void close() {
        log.info("关闭Anthropic客户端");
        clientStatus = ClientStatus.CLOSING;
        // 确保清理任何资源
        clientStatus = ClientStatus.OFFLINE;
    }
    
    public String getName() {
        return "anthropic";
    }

    private Map<String, Object> buildRequestParams(String prompt, Map<String, Object> parameters) {
        Map<String, Object> requestParams = new HashMap<>();

        // 添加模型参数，默认使用配置的默认模型
        String model = parameters != null && parameters.containsKey("model")
                ? (String) parameters.get("model")
                : defaultModel;
        requestParams.put("model", model);

        // 添加系统提示词（如果存在）
        if (parameters != null && parameters.containsKey("system")) {
            requestParams.put("system", parameters.get("system"));
        }

        // 添加用户消息
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
        
        JSONArray messages = new JSONArray();
        messages.add(message);
        requestParams.put("messages", messages);

        // 添加其他参数
        if (parameters != null) {
            if (parameters.containsKey("temperature")) {
                requestParams.put("temperature", parameters.get("temperature"));
            }
            if (parameters.containsKey("max_tokens")) {
                requestParams.put("max_tokens", parameters.get("max_tokens"));
            }
            if (parameters.containsKey("top_p")) {
                requestParams.put("top_p", parameters.get("top_p"));
            }
        }

        return requestParams;
    }
} 