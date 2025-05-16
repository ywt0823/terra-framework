package com.terra.framework.nova.model.client;

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
 * Ollama模型客户端实现
 *
 * @author terra-nova
 */
@Slf4j
public class OllamaClient implements ModelClient {

    private final HttpClientUtils httpClientUtils;
    private final String baseUrl;
    private final String[] supportedModels;
    private volatile ClientStatus clientStatus = ClientStatus.INITIALIZING;

    public OllamaClient(HttpClientUtils httpClientUtils, String baseUrl) {
        this.httpClientUtils = httpClientUtils;
        // 确保baseUrl不以/结尾
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.supportedModels = new String[]{
            "llama3",
            "mistral",
            "mixtral",
            "gemma:7b"
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

            JSONObject response = httpClientUtils.sendPostDataByJson(
                baseUrl + "/api/generate",
                requestBody,
                StandardCharsets.UTF_8,
                contentTypeHeader
            );

            // 解析响应
            if (response != null && response.containsKey("response")) {
                return response.getString("response");
            }

            log.error("Ollama响应格式错误: {}", response);
            clientStatus = ClientStatus.ERROR;
            return null;
        } catch (Exception e) {
            log.error("调用Ollama API失败", e);
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
        // 模拟流式处理
        CompletableFuture.runAsync(() -> {
            try {
                Map<String, Object> requestParams = buildRequestParams(prompt, parameters);
                requestParams.put("stream", true);

                // 此处应实现真正的流式处理，暂时简化处理
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
        log.info("初始化Ollama客户端");

        try {
            // 简单测试以验证连接
            JSONObject testResponse = httpClientUtils.sendGetData(
                baseUrl + "/api/tags",
                StandardCharsets.UTF_8,
                null
            );

            if (testResponse != null && testResponse.containsKey("models")) {
                log.info("Ollama客户端初始化成功");
                clientStatus = ClientStatus.READY;
            } else {
                log.error("Ollama客户端初始化失败: 无法连接API");
                clientStatus = ClientStatus.ERROR;
            }
        } catch (MalformedURLException e) {
            log.error("Ollama客户端初始化失败", e);
            clientStatus = ClientStatus.ERROR;
        }
    }

    @Override
    public void close() {
        log.info("关闭Ollama客户端");
        clientStatus = ClientStatus.CLOSING;
        // 确保清理任何资源
        clientStatus = ClientStatus.OFFLINE;
    }

    private Map<String, Object> buildRequestParams(String prompt, Map<String, Object> parameters) {
        Map<String, Object> requestParams = new HashMap<>();

        // 添加模型参数，默认使用llama3
        String model = parameters != null && parameters.containsKey("model")
            ? (String) parameters.get("model")
            : "llama3";
        requestParams.put("model", model);

        // 添加提示词
        requestParams.put("prompt", prompt);

        // 添加其他参数
        if (parameters != null) {
            if (parameters.containsKey("temperature")) {
                requestParams.put("temperature", parameters.get("temperature"));
            }
            if (parameters.containsKey("max_tokens")) {
                requestParams.put("num_predict", parameters.get("max_tokens"));
            }
            if (parameters.containsKey("top_p")) {
                requestParams.put("top_p", parameters.get("top_p"));
            }
        }

        return requestParams;
    }
}
