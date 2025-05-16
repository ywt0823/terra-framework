package com.terra.framework.nova.llm.model.deepseek;

import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.common.JsonUtils;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.core.ModelConfig;
import com.terra.framework.nova.llm.model.base.AbstractLLMModel;
import com.terra.framework.nova.llm.util.ModelResponseHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

/**
 * DeepSeek模型适配器
 */
@Slf4j
public class DeepSeekModel extends AbstractLLMModel {

    private final ModelConfig config;
    private static final String DEFAULT_MODEL = "deepseek-chat";
    private static final String DEEPSEEK_API_URL = "https://api.deepseek.com/v1";

    public DeepSeekModel(ModelConfig config, HttpClientUtils httpClientUtils) {
        super(httpClientUtils);
        this.config = config;
    }

    @Override
    public void init() {
        log.info("Initializing DeepSeek model with endpoint: {}", config.getApiEndpoint());
        // HttpClientUtils是静态工具类,无需初始化
    }

    @Override
    public String predict(String prompt) {
        try {
            log.debug("Sending prediction request to DeepSeek model with prompt: {}", prompt);

            String baseUrl = config.getApiEndpoint() != null ? config.getApiEndpoint() : DEEPSEEK_API_URL;
            String url = baseUrl + "/chat/completions";

            // 构建请求参数
            Map<String, Object> requestBody = Map.of(
                "model", config.getModelName() != null ? config.getModelName() : DEFAULT_MODEL,
                "messages", new Object[]{Map.of("role", "user", "content", prompt)},
                "temperature", config.getTemperature(),
                "max_tokens", config.getMaxTokens(),
                "stream", false
            );

            // 构建请求头
            Header[] headers = new BasicHeader[]{
                new BasicHeader("Authorization", config.getApiKey()),
                new BasicHeader("Content-Type", "application/json")
            };
            // 发送请求
            JSONObject result = httpClientUtils.sendPostDataByJson(url, JsonUtils.objectCovertToJson(requestBody), StandardCharsets.UTF_8, headers);

            return extractContent(result);

        } catch (Exception e) {
            log.error("Error during DeepSeek prediction", e);
            throw new RuntimeException("DeepSeek prediction failed", e);
        }
    }

    @Override
    public Publisher<String> predictStream(String prompt) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> predict(prompt));
        return ModelResponseHandler.createStreamPublisher(future);
    }

    private String extractContent(Map<String, Object> response) {
        try {
            Map<String, Object> choice = ((java.util.List<Map<String, Object>>) response.get("choices")).get(0);
            Map<String, Object> message = (Map<String, Object>) choice.get("message");
            return (String) message.get("content");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract content from response", e);
        }
    }

    @Override
    public void close() {
        log.info("Closing DeepSeek model");
        // HttpClientUtils是静态工具类,无需关闭
    }
}
