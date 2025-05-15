package com.terra.framework.stream.core;

import java.util.Map;

/**
 * 消息接口
 * 定义消息的基本结构
 *
 * @author terra
 */
public interface StreamMessage<T> {
    /**
     * 获取消息唯一标识
     */
    String getId();
    
    /**
     * 获取消息负载内容
     */
    T getPayload();
    
    /**
     * 获取消息头信息
     */
    Map<String, Object> getHeaders();
    
    /**
     * 获取消息时间戳
     */
    Long getTimestamp();
} 