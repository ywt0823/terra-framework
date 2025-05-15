package com.terra.framework.stream.factory;

import com.terra.framework.stream.core.MessageQueue;

/**
 * 消息队列工厂接口
 * 
 * @author terra
 */
public interface MessageQueueFactory {
    /**
     * 获取指定类型的消息队列
     *
     * @param type 队列类型，如"rabbitmq"或"redis"
     * @return 消息队列实例
     */
    MessageQueue getMessageQueue(String type);
} 