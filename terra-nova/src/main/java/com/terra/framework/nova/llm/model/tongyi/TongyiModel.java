package com.terra.framework.nova.llm.model.tongyi;

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
 * 通义千问模型适配器
 */
@Slf4j
public class TongyiModel extends AbstractLLMModel {

    private final ModelConfig config;
    private static final String DEFAULT_MODEL = "qwen-turbo";
    private static final String TONGYI_API_URL = "https://dashscope.aliyuncs.com/v1";

    public TongyiModel(ModelConfig config, HttpClientUtils httpClientUtils) {
        super(httpClientUtils);
        this.config = config;
    }

    @Override
    public void init() {
        log.info("Initializing Tongyi model with endpoint: {}", config.getApiEndpoint());
    }

    @Override
    public String predict(String prompt) {
        try {
            log.debug("Sending prediction request to Tongyi model with prompt: {}", prompt);

            String baseUrl = config.getApiEndpoint() != null ? config.getApiEndpoint() : TONGYI_API_URL;
            String url = baseUrl + "/services/chat/completion";

            // 构建请求参数
            Map<String, Object> requestBody = Map.of(
                "model", config.getModelName() != null ? config.getModelName() : DEFAULT_MODEL,
                "messages", new Object[]{Map.of("role", "user", "content", prompt)},
                "parameters", Map.of(
                    "temperature", config.getTemperature(),
                    "max_tokens", config.getMaxTokens(),
                    "result_format", "text"
                )
            );

            Header[] headers = new BasicHeader[]{
                new BasicHeader("Authorization", config.getApiKey()),
                new BasicHeader("Content-Type", "application/json")
            };

            JSONObject result = httpClientUtils.sendPostDataByJson(url, JsonUtils.objectCovertToJson(requestBody), StandardCharsets.UTF_8, headers);

            return extractContent(result);

        } catch (Exception e) {
            log.error("Error during Tongyi prediction", e);
            throw new RuntimeException("Tongyi prediction failed", e);
        }
    }

    @Override
    public Publisher<String> predictStream(String prompt) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> predict(prompt));
        return ModelResponseHandler.createStreamPublisher(future);
    }

    private String extractContent(Map<String, Object> response) {
        try {
            Map<String, Object> output = (Map<String, Object>) response.get("output");
            return (String) output.get("text");
        } catch (Exception e) {
            throw new RuntimeException("Failed to extract content from response", e);
        }
    }

    @Override
    public void close() {
        log.info("Closing Tongyi model");
        // HttpClientUtils是共享实例,不需要关闭
    }
}
