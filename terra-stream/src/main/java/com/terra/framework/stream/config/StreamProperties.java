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
     * Redis Stream配置
     */
    private final Redis redis = new Redis();
    
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
    
    /**
     * Redis Stream配置属性
     */
    @Data
    public static class Redis {
        /**
         * 是否启用Redis Stream
         */
        private boolean enabled = true;
        
        /**
         * Stream最大长度
         * 当超过此长度时，会删除最旧的消息
         * 值为0表示不限制长度
         */
        private long maxLen = 1000000;
        
        /**
         * 消费者组自动创建
         * 如果消费者组不存在，是否自动创建
         */
        private boolean autoCreateGroup = true;
        
        /**
         * 消费者名称前缀
         */
        private String consumerNamePrefix = "consumer-";
        
        /**
         * 消费者超时时间（毫秒）
         * 用于阻塞读取时的超时时间
         */
        private long consumerTimeout = 1000;
        
        /**
         * 批量消费数量
         * 一次从Stream中读取的最大消息数量
         */
        private int batchSize = 10;
        
        /**
         * 消息确认模式
         * AUTO: 自动确认
         * MANUAL: 手动确认
         */
        private String ackMode = "AUTO";
        
        /**
         * 消费者拉取消息间隔（毫秒）
         */
        private long pollInterval = 100;
    }
} 