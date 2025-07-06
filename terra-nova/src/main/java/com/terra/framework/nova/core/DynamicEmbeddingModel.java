package com.terra.framework.nova.core;

import org.springframework.ai.document.Document;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.embedding.EmbeddingRequest;
import org.springframework.ai.embedding.EmbeddingResponse;
import org.springframework.util.Assert;

import java.util.Collections;
import java.util.Map;

/**
 * 动态 {@link org.springframework.ai.embedding.EmbeddingModel} 实现。
 * <p>
 * 该客户端作为一个代理，将嵌入请求路由到在 {@link ModelProviderContextHolder} 中指定的具体 {@link org.springframework.ai.embedding.EmbeddingModel} 实例。
 * 如果上下文中没有指定模型ID，则会使用默认的客户端。
 *
 * @author DeavyJones
 * @version 1.0.0
 * @since 1.0.0
 */
public class DynamicEmbeddingModel implements EmbeddingModel {

    private final Map<String, EmbeddingModel> embeddingClientMap;
    private final String defaultClient;

    /**
     * 构造一个新的 DynamicEmbeddingClient。
     *
     * @param embeddingClientMap 包含所有可用 EmbeddingClient 的映射，键为 Bean 名称，值为客户端实例。
     * @param defaultClient      默认的 EmbeddingClient Bean 名称，在未指定模型ID时使用。
     */
    public DynamicEmbeddingModel(Map<String, EmbeddingModel> embeddingClientMap, String defaultClient) {
        Assert.notNull(embeddingClientMap, "embeddingClientMap must not be null");
        Assert.notNull(defaultClient, "defaultClient must not be null");
        this.embeddingClientMap = Collections.unmodifiableMap(embeddingClientMap);
        this.defaultClient = defaultClient;
    }

    @Override
    public float[] embed(String text) {
        return getTargetClient().embed(text);
    }

    @Override
    public float[] embed(Document document) {
        return getTargetClient().embed(document);
    }

    @Override
    public EmbeddingResponse call(EmbeddingRequest request) {
        return getTargetClient().call(request);
    }

    /**
     * 根据上下文获取目标 EmbeddingClient。
     *
     * @return 目标 EmbeddingClient 实例。
     * @throws IllegalArgumentException 如果找不到指定的客户端。
     */
    private EmbeddingModel getTargetClient() {
        String modelId = ModelProviderContextHolder.getModelId();
        if (modelId == null) {
            modelId = this.defaultClient;
        }
        EmbeddingModel client = this.embeddingClientMap.get(modelId);
        if (client == null) {
            throw new IllegalArgumentException(String.format(
                "未找到ID为 '%s' 的 EmbeddingClient Bean。可用ID: %s",
                modelId, this.embeddingClientMap.keySet()
            ));
        }
        return client;
    }
}
