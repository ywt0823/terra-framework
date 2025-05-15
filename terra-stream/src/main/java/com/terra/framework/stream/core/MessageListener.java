package com.terra.framework.stream.core;

/**
 * 消息监听器接口
 * 
 * @author terra
 */
public interface MessageListener<T> {
    /**
     * 处理消息
     *
     * @param message 消息对象
     */
    void onMessage(StreamMessage<T> message);
} 