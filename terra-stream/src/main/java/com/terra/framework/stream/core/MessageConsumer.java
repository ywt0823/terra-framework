package com.terra.framework.stream.core;

/**
 * 消息消费者接口
 * 
 * @author terra
 */
public interface MessageConsumer {
    /**
     * 订阅队列
     *
     * @param destination 目标队列
     * @param listener 消息监听器
     * @param <T> 消息负载类型
     */
    <T> void subscribe(String destination, MessageListener<T> listener);
    
    /**
     * 取消订阅
     *
     * @param destination 目标队列
     */
    void unsubscribe(String destination);
} 