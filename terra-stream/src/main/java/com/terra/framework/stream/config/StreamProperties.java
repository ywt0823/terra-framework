package com.terra.framework.stream.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 消息流配置属性
 * 
 * @author terra
 */
@Data
@ConfigurationProperties(prefix = "terra.stream")
public class StreamProperties {
    /**
     * 是否启用
     */
    private boolean enabled = true;
    
    /**
     * 默认队列类型
     */
    private String defaultQueueType = "rabbitmq"; // rabbitmq 或 redis
    
    /**
     * RabbitMQ配置
     */
    private final RabbitMq rabbitMq = new RabbitMq();
    
    /**
     * RabbitMQ配置属性
     */
    @Data
    public static class RabbitMq {
        /**
         * 是否启用RabbitMQ
         */
        private boolean enabled = true;
        
        /**
         * 队列是否持久化
         */
        private boolean durable = true;
        
        /**
         * 是否排他队列
         */
        private boolean exclusive = false;
        
        /**
         * 是否自动删除
         */
        private boolean autoDelete = false;
        
        /**
         * 并发消费者数量
         */
        private int concurrentConsumers = 1;
        
        /**
         * 最大并发消费者数量
         */
        private int maxConcurrentConsumers = 10;
        
        /**
         * 预取数量
         */
        private int prefetchCount = 250;
    }
} 