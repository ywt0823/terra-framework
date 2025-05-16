package com.terra.framework.nova.model.client;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.net.MalformedURLException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * OpenAI模型客户端实现
 *
 * @author terra-nova
 */
@Slf4j
public class OpenAIClient implements ModelClient {

    private static final String API_URL = "https://api.openai.com/v1/chat/completions";

    private final HttpClientUtils httpClientUtils;
    private final String apiKey;
    private final String[] supportedModels;
    private volatile ClientStatus clientStatus = ClientStatus.INITIALIZING;

    public OpenAIClient(HttpClientUtils httpClientUtils, String apiKey) {
        this.httpClientUtils = httpClientUtils;
        this.apiKey = apiKey;
        this.supportedModels = new String[]{
                "gpt-3.5-turbo",
                "gpt-4",
                "gpt-4-turbo",
                "gpt-4o"
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
            Header authHeader = new BasicHeader("Authorization", "Bearer " + apiKey);

            JSONObject response = httpClientUtils.sendPostDataByJson(
                    API_URL,
                    requestBody,
                    StandardCharsets.UTF_8,
                    contentTypeHeader, authHeader
            );

            // 解析响应
            if (response != null && response.containsKey("choices")) {
                JSONArray choices = response.getJSONArray("choices");
                if (!choices.isEmpty()) {
                    JSONObject choice = choices.getJSONObject(0);
                    JSONObject message = choice.getJSONObject("message");
                    return message.getString("content");
                }
            }

            log.error("OpenAI响应格式错误: {}", response);
            clientStatus = ClientStatus.ERROR;
            return null;
        } catch (Exception e) {
            log.error("调用OpenAI API失败", e);
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
        // 流式API实现较复杂，需要用HTTP流式读取，这里使用异步方式模拟
        // 实际项目中应当实现真正的流式响应
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
        log.info("初始化OpenAI客户端");

        try {
            // 简单测试以验证连接
            JSONObject testResponse = httpClientUtils.sendGetData(
                    "https://api.openai.com/v1/models",
                    StandardCharsets.UTF_8,
                    apiKey
            );

            if (testResponse != null && testResponse.containsKey("data")) {
                log.info("OpenAI客户端初始化成功");
                clientStatus = ClientStatus.READY;
            } else {
                log.error("OpenAI客户端初始化失败: 无法连接API");
                clientStatus = ClientStatus.ERROR;
            }
        } catch (MalformedURLException e) {
            log.error("OpenAI客户端初始化失败", e);
            clientStatus = ClientStatus.ERROR;
        }
    }

    @Override
    public void close() {
        log.info("关闭OpenAI客户端");
        clientStatus = ClientStatus.CLOSING;
        // 确保清理任何资源
        clientStatus = ClientStatus.OFFLINE;
    }

    private Map<String, Object> buildRequestParams(String prompt, Map<String, Object> parameters) {
        Map<String, Object> requestParams = new HashMap<>();

        // 添加模型参数，默认使用gpt-3.5-turbo
        String model = parameters != null && parameters.containsKey("model")
                ? (String) parameters.get("model")
                : "gpt-3.5-turbo";
        requestParams.put("model", model);

        // 构建消息数组
        JSONArray messages = new JSONArray();
        JSONObject message = new JSONObject();
        message.put("role", "user");
        message.put("content", prompt);
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
