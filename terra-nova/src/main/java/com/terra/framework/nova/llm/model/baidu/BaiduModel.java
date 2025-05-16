package com.terra.framework.nova.llm.model.baidu;

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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

/**
 * 百度文心一言模型实现
 */
@Slf4j
public class BaiduModel extends AbstractLLMModel {

    private final ModelConfig config;
    private String accessToken;
    private static final String DEFAULT_MODEL = "ernie-bot-4";
    private static final String TOKEN_URL = "https://aip.baidubce.com/oauth/2.0/token";

    public BaiduModel(ModelConfig config, HttpClientUtils httpClientUtils) {
        super(httpClientUtils);
        this.config = config;
    }

    @Override
    public void init() {
        log.info("初始化百度文心一言模型, 端点: {}", config.getApiEndpoint());
        refreshAccessToken();
    }

    @Override
    public String predict(String prompt) {
        try {
            log.debug("发送预测请求到百度文心一言模型, 提示: {}", prompt);

            String url = config.getApiEndpoint() + "/" + getModelName();

            List<Map<String, String>> messages = new ArrayList<>();
            messages.add(Map.of("role", "user", "content", prompt));

            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("messages", messages);
            requestBody.put("temperature", config.getTemperature());
            requestBody.put("top_p", 0.8);
            requestBody.put("max_output_tokens", config.getMaxTokens());

            Header[] headers = new Header[]{
                    new BasicHeader("Content-Type", "application/json"),
                    new BasicHeader("Authorization", "Bearer " + accessToken)
            };

            JSONObject result = httpClientUtils.sendPostDataByJson(
                    url,
                    JsonUtils.objectCovertToJson(requestBody),
                    StandardCharsets.UTF_8,
                    headers
            );

            return extractContent(result);

        } catch (Exception e) {
            log.error("百度文心一言请求失败", e);
            throw new RuntimeException("百度文心一言请求失败", e);
        }
    }

    @Override
    public Publisher<String> predictStream(String prompt) {
        CompletableFuture<String> future = CompletableFuture.supplyAsync(() -> predict(prompt));
        return ModelResponseHandler.createStreamPublisher(future);
    }

    private String getModelName() {
        return config.getModelName() != null ? config.getModelName() : DEFAULT_MODEL;
    }

    private void refreshAccessToken() {
        try {
            String apiKey = config.getApiKey();
            String secretKey = (String) config.getExtraParams().get("secretKey");

            if (apiKey == null || secretKey == null) {
                throw new IllegalArgumentException("百度文心一言需要配置apiKey和secretKey");
            }

            Map<String, String> params = Map.of(
                    "grant_type", "client_credentials",
                    "client_id", apiKey,
                    "client_secret", secretKey
            );

            JSONObject result = httpClientUtils.sendPostByFormData(TOKEN_URL, params, StandardCharsets.UTF_8, "");
            accessToken = result.getString("access_token");

            if (accessToken == null) {
                throw new RuntimeException("获取百度文心一言访问令牌失败");
            }

            log.info("百度文心一言访问令牌已获取");

        } catch (Exception e) {
            log.error("获取百度文心一言访问令牌失败", e);
            throw new RuntimeException("获取百度文心一言访问令牌失败", e);
        }
    }

    private String extractContent(Map<String, Object> response) {
        try {
            Map<String, Object> result = (Map<String, Object>) response.get("result");
            return (String) result.get("content");
        } catch (Exception e) {
            throw new RuntimeException("解析百度文心一言响应失败", e);
        }
    }

    @Override
    public void close() {
        log.info("关闭百度文心一言模型");
        // HttpClientUtils是共享实例,不需要关闭
    }
}
