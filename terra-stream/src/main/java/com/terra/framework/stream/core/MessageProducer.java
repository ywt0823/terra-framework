package com.terra.framework.stream.core;

import java.util.Map;

/**
 * 消息生产者接口
 * 
 * @author terra
 */
public interface MessageProducer {
    /**
     * 发送消息
     *
     * @param destination 目标队列
     * @param message 消息对象
     * @param <T> 消息负载类型
     */
    <T> void send(String destination, StreamMessage<T> message);
    
    /**
     * 发送消息
     *
     * @param destination 目标队列
     * @param payload 消息负载
     * @param <T> 消息负载类型
     */
    <T> void send(String destination, T payload);
    
    /**
     * 发送消息
     *
     * @param destination 目标队列
     * @param payload 消息负载
     * @param headers 消息头
     * @param <T> 消息负载类型
     */
    <T> void send(String destination, T payload, Map<String, Object> headers);
} 