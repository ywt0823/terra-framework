package com.terra.framework.stream.redis;

import com.terra.framework.stream.config.StreamProperties;
import com.terra.framework.stream.core.DefaultStreamMessage;
import com.terra.framework.stream.core.MessageConsumer;
import com.terra.framework.stream.core.MessageListener;
import com.terra.framework.stream.core.StreamMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.stream.StreamMessageListenerContainer;
import org.springframework.data.redis.stream.Subscription;

import java.time.Duration;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Redis Stream消息消费者实现
 * 
 * @author terra
 */
@Slf4j
public class RedisStreamMessageConsumer implements MessageConsumer {

    private final StringRedisTemplate redisTemplate;
    private final StreamProperties.Redis properties;
    private final String groupId;
    private final Map<String, Subscription> subscriptions = new ConcurrentHashMap<>();
    private final Map<String, PollTask> pollTasks = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public RedisStreamMessageConsumer(StringRedisTemplate redisTemplate, 
                                     StreamProperties.Redis properties, 
                                     String groupId) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
        this.groupId = groupId;
    }

    @Override
    public <T> void subscribe(String destination, MessageListener<T> listener) {
        // 确保Stream和消费者组存在
        ensureStreamAndGroupExist(destination, groupId);
        
        // 创建消费者ID
        String consumerId = properties.getConsumerNamePrefix() + UUID.randomUUID().toString();
        
        if ("AUTO".equalsIgnoreCase(properties.getAckMode())) {
            // 自动确认模式
            createSubscription(destination, consumerId, listener);
        } else {
            // 手动拉取模式
            createPollTask(destination, consumerId, listener);
        }
        
        log.info("已订阅Redis Stream: {}, 消费组: {}, 消费者: {}", destination, groupId, consumerId);
    }

    @Override
    public void unsubscribe(String destination) {
        Subscription subscription = subscriptions.remove(destination);
        if (subscription != null) {
            subscription.cancel();
            log.info("已取消订阅Redis Stream: {}", destination);
        }
        
        PollTask pollTask = pollTasks.remove(destination);
        if (pollTask != null) {
            pollTask.stop();
            log.info("已停止Redis Stream轮询任务: {}", destination);
        }
    }
    
    /**
     * 确保Stream和消费者组存在
     * 
     * @param destination Stream名称
     * @param groupId 消费者组ID
     */
    private void ensureStreamAndGroupExist(String destination, String groupId) {
        try {
            // 检查Stream是否存在
            try {
                redisTemplate.opsForStream().info(destination);
            } catch (Exception e) {
                // Stream不存在，创建一个初始化消息
                Map<String, String> initMessage = new HashMap<>();
                initMessage.put("init", "true");
                
                RecordId recordId = redisTemplate.opsForStream().add(destination, initMessage);
                log.info("创建Redis Stream: {}", destination);
            }
            
            // 尝试创建消费者组
            try {
                redisTemplate.opsForStream().createGroup(destination, groupId);
                log.info("创建Redis Stream消费者组: {}, Stream: {}", groupId, destination);
            } catch (Exception e) {
                // 消费者组可能已存在
                log.debug("Redis Stream消费者组可能已存在: {}, Stream: {}", groupId, destination);
            }
        } catch (Exception e) {
            log.error("创建Redis Stream或消费者组失败: {}, {}", destination, groupId, e);
            throw new RuntimeException("无法创建Stream或消费者组", e);
        }
    }
    
    /**
     * 创建订阅（适用于自动确认模式）
     */
    @SuppressWarnings("unchecked")
    private <T> void createSubscription(String destination, String consumerId, MessageListener<T> listener) {
        try {
            StreamMessageListenerContainer.StreamReadRequest<String, MapRecord<String, String, String>> readRequest =
                    StreamMessageListenerContainer.StreamReadRequest
                            .builder(StreamOffset.create(destination, ReadOffset.lastConsumed()))
                            .consumerName(consumerId)
                            .consumerGroup(groupId)
                            .autoAcknowledge(true)
                            .build();
            
            // TODO: 这里我们需要实现一个适配器，将Spring Data Redis的StreamListener转换为我们的MessageListener
            // 目前这里只是占位，实际项目中需要实现适配器
            // 这里留作后续实现
            log.warn("Redis Stream自动确认模式订阅功能尚未完全实现");
        } catch (Exception e) {
            log.error("创建Redis Stream订阅失败: {}", destination, e);
            throw new RuntimeException("无法创建订阅", e);
        }
    }
    
    /**
     * 创建轮询任务（适用于手动确认模式）
     */
    private <T> void createPollTask(String destination, String consumerId, MessageListener<T> listener) {
        PollTask<T> task = new PollTask<>(destination, consumerId, listener);
        pollTasks.put(destination, task);
        
        // 启动轮询任务
        scheduler.scheduleAtFixedRate(
                task,
                0,
                properties.getPollInterval(),
                TimeUnit.MILLISECONDS
        );
    }
    
    /**
     * 轮询任务，定期从Redis Stream中拉取消息
     */
    private class PollTask<T> implements Runnable {
        private final String destination;
        private final String consumerId;
        private final MessageListener<T> listener;
        private volatile boolean running = true;
        
        public PollTask(String destination, String consumerId, MessageListener<T> listener) {
            this.destination = destination;
            this.consumerId = consumerId;
            this.listener = listener;
        }
        
        @Override
        public void run() {
            if (!running) {
                return;
            }
            
            try {
                // 从Stream中拉取消息
                List<MapRecord<String, String, String>> records = redisTemplate.opsForStream()
                        .read(Consumer.from(groupId, consumerId),
                                StreamReadOptions.empty()
                                        .count(properties.getBatchSize())
                                        .block(Duration.ofMillis(properties.getConsumerTimeout())),
                                StreamOffset.create(destination, ReadOffset.lastConsumed()));
                
                if (records != null && !records.isEmpty()) {
                    for (MapRecord<String, String, String> record : records) {
                        processRecord(record);
                        
                        // 确认消息
                        redisTemplate.opsForStream().acknowledge(destination, groupId, record.getId());
                    }
                }
            } catch (Exception e) {
                if (running) {
                    log.error("Redis Stream消息轮询失败: {}", destination, e);
                }
            }
        }
        
        @SuppressWarnings("unchecked")
        private void processRecord(MapRecord<String, String, String> record) {
            try {
                Map<String, String> values = record.getValue();
                
                // 提取消息ID和时间戳
                String id = values.getOrDefault("id", record.getId().getValue());
                long timestamp = Long.parseLong(values.getOrDefault("timestamp", String.valueOf(System.currentTimeMillis())));
                
                // 提取负载
                String payloadJson = values.get("payload");
                // 这里假设类型为Object，实际上需要根据具体类型进行反序列化
                // 这是一个简化处理，在实际场景中可能需要处理类型信息
                T payload = (T) RedisStreamSerializer.deserialize(payloadJson, Object.class);
                
                // 提取消息头
                Map<String, Object> headers = new HashMap<>();
                for (Map.Entry<String, String> entry : values.entrySet()) {
                    if (entry.getKey().startsWith("header:")) {
                        String headerName = entry.getKey().substring("header:".length());
                        headers.put(headerName, entry.getValue());
                    }
                }
                
                // 创建消息对象
                StreamMessage<T> message = DefaultStreamMessage.<T>builder()
                        .id(id)
                        .payload(payload)
                        .headers(headers)
                        .timestamp(timestamp)
                        .build();
                
                // 调用监听器处理消息
                listener.onMessage(message);
            } catch (Exception e) {
                log.error("处理Redis Stream消息失败: {}", record.getId(), e);
                throw new RuntimeException("处理消息失败", e);
            }
        }
        
        public void stop() {
            running = false;
        }
    }
} 