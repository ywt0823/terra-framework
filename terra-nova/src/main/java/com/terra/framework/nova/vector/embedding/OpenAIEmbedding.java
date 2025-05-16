package com.terra.framework.nova.vector.embedding;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.message.BasicHeader;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;

/**
 * OpenAI嵌入服务实现
 *
 * @author terra-nova
 */
@Slf4j
public class OpenAIEmbedding implements EmbeddingService {

    private static final String API_URL = "https://api.openai.com/v1/embeddings";
    private static final int DEFAULT_DIMENSION = 1536; // OpenAI text-embedding-ada-002 默认维度
    private static final int EMBEDDING_3_DIMENSION = 3072; // text-embedding-3-small 默认维度

    private final HttpClientUtils httpClientUtils;
    private final String apiKey;
    private final String model;
    private final int dimension;
    private final int batchSize;

    /**
     * 构造函数
     *
     * @param httpClientUtils HTTP客户端工具
     * @param apiKey OpenAI API密钥
     * @param model 模型名称
     * @param batchSize 批处理大小
     */
    public OpenAIEmbedding(HttpClientUtils httpClientUtils, String apiKey, String model, int batchSize) {
        this.httpClientUtils = httpClientUtils;
        this.apiKey = apiKey;
        this.model = model != null ? model : "text-embedding-3-small";
        this.batchSize = Math.max(1, Math.min(batchSize, 100)); // OpenAI限制单次请求最多100个文本

        // 根据模型设置维度
        if (this.model.contains("text-embedding-3")) {
            this.dimension = EMBEDDING_3_DIMENSION;
        } else {
            this.dimension = DEFAULT_DIMENSION;
        }

        log.info("初始化OpenAI嵌入服务，模型: {}, 维度: {}, 批处理大小: {}", this.model, this.dimension, this.batchSize);
    }

    @Override
    public float[] embed(String text) {
        if (text == null || text.trim().isEmpty()) {
            return new float[getDimension()];
        }

        try {
            Map<String, Object> requestBody = new HashMap<>();
            requestBody.put("model", model);
            requestBody.put("input", text);

            Header contentTypeHeader = new BasicHeader("Content-Type", "application/json");
            Header authHeader = new BasicHeader("Authorization", "Bearer " + apiKey);

            JSONObject response = httpClientUtils.sendPostDataByJson(
                    API_URL,
                    JSONObject.toJSONString(requestBody),
                    StandardCharsets.UTF_8,
                    contentTypeHeader, authHeader
            );

            return parseEmbedding(response);
        } catch (Exception e) {
            log.error("生成嵌入向量失败", e);
            return new float[getDimension()];
        }
    }

    @Override
    public List<float[]> embedBatch(List<String> texts) {
        if (texts == null || texts.isEmpty()) {
            return new ArrayList<>();
        }

        List<float[]> embeddings = new ArrayList<>(texts.size());

        // 分批处理，避免超过API限制
        for (int i = 0; i < texts.size(); i += batchSize) {
            int end = Math.min(i + batchSize, texts.size());
            List<String> batch = texts.subList(i, end);

            try {
                Map<String, Object> requestBody = new HashMap<>();
                requestBody.put("model", model);
                requestBody.put("input", batch);

                Header contentTypeHeader = new BasicHeader("Content-Type", "application/json");
                Header authHeader = new BasicHeader("Authorization", "Bearer " + apiKey);

                JSONObject response = httpClientUtils.sendPostDataByJson(
                        API_URL,
                        JSONObject.toJSONString(requestBody),
                        StandardCharsets.UTF_8,
                        contentTypeHeader, authHeader
                );

                List<float[]> batchEmbeddings = parseBatchEmbeddings(response);
                embeddings.addAll(batchEmbeddings);
            } catch (Exception e) {
                log.error("批量生成嵌入向量失败", e);
                // 为失败的批次添加空向量
                for (int j = 0; j < batch.size(); j++) {
                    embeddings.add(new float[getDimension()]);
                }
            }
        }

        return embeddings;
    }

    @Override
    public CompletableFuture<List<float[]>> embedBatchAsync(List<String> texts) {
        return CompletableFuture.supplyAsync(() -> embedBatch(texts), Executors.newSingleThreadExecutor());
    }

    @Override
    public int getDimension() {
        return dimension;
    }

    /**
     * 解析单个嵌入向量响应
     *
     * @param response API响应
     * @return 嵌入向量
     */
    private float[] parseEmbedding(JSONObject response) {
        if (response == null || !response.containsKey("data") || response.getJSONArray("data").isEmpty()) {
            log.warn("无效的嵌入向量响应");
            return new float[getDimension()];
        }

        JSONObject data = response.getJSONArray("data").getJSONObject(0);
        JSONArray embeddingArray = data.getJSONArray("embedding");

        float[] embedding = new float[embeddingArray.size()];
        for (int i = 0; i < embeddingArray.size(); i++) {
            embedding[i] = embeddingArray.getFloatValue(i);
        }

        return embedding;
    }

    /**
     * 解析批量嵌入向量响应
     *
     * @param response API响应
     * @return 嵌入向量列表
     */
    private List<float[]> parseBatchEmbeddings(JSONObject response) {
        if (response == null || !response.containsKey("data")) {
            log.warn("无效的批量嵌入向量响应");
            return new ArrayList<>();
        }

        JSONArray dataArray = response.getJSONArray("data");
        List<float[]> embeddings = new ArrayList<>(dataArray.size());

        for (int i = 0; i < dataArray.size(); i++) {
            JSONObject data = dataArray.getJSONObject(i);
            JSONArray embeddingArray = data.getJSONArray("embedding");

            float[] embedding = new float[embeddingArray.size()];
            for (int j = 0; j < embeddingArray.size(); j++) {
                embedding[j] = embeddingArray.getFloatValue(j);
            }

            embeddings.add(embedding);
        }

        return embeddings;
    }
}
