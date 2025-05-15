package com.terra.framework.stream.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 默认消息实现
 *
 * @author terra
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DefaultStreamMessage<T> implements StreamMessage<T> {
    private String id;
    private T payload;
    private Map<String, Object> headers;
    private Long timestamp;

    /**
     * 创建消息
     *
     * @param payload 消息负载
     * @param <T> 负载类型
     * @return 消息对象
     */
    public static <T> DefaultStreamMessage<T> create(T payload) {
        return DefaultStreamMessage.<T>builder()
                .id(UUID.randomUUID().toString())
                .payload(payload)
                .headers(new HashMap<>())
                .timestamp(System.currentTimeMillis())
                .build();
    }

    /**
     * 创建消息
     *
     * @param payload 消息负载
     * @param headers 消息头
     * @param <T> 负载类型
     * @return 消息对象
     */
    public static <T> DefaultStreamMessage<T> create(T payload, Map<String, Object> headers) {
        return DefaultStreamMessage.<T>builder()
                .id(UUID.randomUUID().toString())
                .payload(payload)
                .headers(headers)
                .timestamp(System.currentTimeMillis())
                .build();
    }
} 