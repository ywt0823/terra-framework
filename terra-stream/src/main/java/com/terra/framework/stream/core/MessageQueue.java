package com.terra.framework.stream.core;

import java.util.List;

/**
 * 消息队列接口
 * 
 * @author terra
 */
public interface MessageQueue {
    /**
     * 创建消息生产者
     *
     * @return 消息生产者
     */
    MessageProducer createProducer();
    
    /**
     * 创建消息消费者
     *
     * @param groupId 消费组ID
     * @return 消息消费者
     */
    MessageConsumer createConsumer(String groupId);
    
    /**
     * 创建队列
     *
     * @param name 队列名称
     */
    void createQueue(String name);
    
    /**
     * 删除队列
     *
     * @param name 队列名称
     */
    void deleteQueue(String name);
    
    /**
     * 获取所有队列
     *
     * @return 队列列表
     */
    List<String> listQueues();
} 