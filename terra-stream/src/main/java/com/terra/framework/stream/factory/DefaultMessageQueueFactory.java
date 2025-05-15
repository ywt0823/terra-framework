package com.terra.framework.stream.factory;

import com.terra.framework.stream.core.MessageQueue;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 默认消息队列工厂实现
 * 
 * @author terra
 */
@Slf4j
public class DefaultMessageQueueFactory implements MessageQueueFactory {
    
    private final Map<String, MessageQueue> messageQueues;
    private final String defaultQueueType;

    public DefaultMessageQueueFactory(Map<String, MessageQueue> messageQueues, String defaultQueueType) {
        this.messageQueues = new ConcurrentHashMap<>(messageQueues);
        this.defaultQueueType = defaultQueueType;
    }

    @Override
    public MessageQueue getMessageQueue(String type) {
        String queueType = type != null && !type.isEmpty() ? type : defaultQueueType;
        
        MessageQueue queue = messageQueues.get(queueType);
        if (queue == null) {
            throw new IllegalArgumentException("没有找到类型为 [" + queueType + "] 的消息队列实现");
        }
        return queue;
    }
    
    /**
     * 注册消息队列实现
     *
     * @param type 队列类型
     * @param messageQueue 消息队列实现
     */
    public void registerMessageQueue(String type, MessageQueue messageQueue) {
        messageQueues.put(type, messageQueue);
        log.info("注册了类型为 [{}] 的消息队列", type);
    }
} 