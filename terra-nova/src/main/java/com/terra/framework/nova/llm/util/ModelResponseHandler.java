package com.terra.framework.nova.llm.util;

import lombok.extern.slf4j.Slf4j;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;
import java.util.concurrent.Flow.Subscriber;

/**
 * 模型响应处理工具类
 */
@Slf4j
public class ModelResponseHandler {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 处理JSON响应
     */
    public static Map<String, Object> handleJsonResponse(String response) {
        try {
            return objectMapper.readValue(response, Map.class);
        } catch (Exception e) {
            log.error("Failed to parse JSON response", e);
            throw new RuntimeException("JSON parsing failed", e);
        }
    }

    /**
     * 创建流式响应发布者
     */
    public static Publisher<String> createStreamPublisher(CompletableFuture<String> future) {
        return new Publisher<String>() {
            @Override
            public void subscribe(Subscriber<? super String> subscriber) {
                future.thenAccept(response -> {
                    try {
                        subscriber.onNext(response);
                        subscriber.onComplete();
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }).exceptionally(e -> {
                    subscriber.onError(e);
                    return null;
                });
            }
        };
    }

    /**
     * 处理错误响应
     */
    public static RuntimeException handleErrorResponse(String message, Throwable cause) {
        log.error("Model error: {}", message, cause);
        return new RuntimeException(message, cause);
    }

    /**
     * 验证响应
     */
    public static void validateResponse(String response) {
        if (response == null || response.trim().isEmpty()) {
            throw new IllegalArgumentException("Empty response from model");
        }
    }
} 