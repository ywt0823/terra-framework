package com.terra.framework.stream.redis;

import com.terra.framework.stream.config.StreamProperties;
import com.terra.framework.stream.core.DefaultStreamMessage;
import com.terra.framework.stream.core.MessageProducer;
import com.terra.framework.stream.core.StreamMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.stream.*;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Redis Stream消息生产者实现
 * 
 * @author terra
 */
@Slf4j
public class RedisStreamMessageProducer implements MessageProducer {

    private final StringRedisTemplate redisTemplate;
    private final StreamProperties.Redis properties;

    public RedisStreamMessageProducer(StringRedisTemplate redisTemplate, StreamProperties.Redis properties) {
        this.redisTemplate = redisTemplate;
        this.properties = properties;
    }

    @Override
    public <T> void send(String destination, StreamMessage<T> message) {
        try {
            // 确保Stream存在
            ensureStreamExists(destination);
            
            // 准备Stream消息字段
            Map<String, String> fields = new HashMap<>();
            
            // 添加消息ID和时间戳
            fields.put("id", message.getId());
            fields.put("timestamp", String.valueOf(message.getTimestamp()));
            
            // 添加负载
            fields.put("payload", RedisStreamSerializer.serialize(message.getPayload()));
            
            // 添加消息头
            for (Map.Entry<String, Object> entry : message.getHeaders().entrySet()) {
                if (entry.getValue() != null) {
                    fields.put("header:" + entry.getKey(), String.valueOf(entry.getValue()));
                }
            }
            
            // 发送消息到Redis Stream
            RecordId recordId = redisTemplate.opsForStream().add(
                    StreamRecords.newRecord()
                            .ofMap(fields)
                            .withStreamKey(destination)
            );
            
            // 设置长度限制
            if (properties.getMaxLen() > 0) {
                // 修剪Stream，保持在配置的最大长度以内
                redisTemplate.opsForStream().trim(destination, properties.getMaxLen());
            }
            
            log.debug("发送消息到Redis Stream {}: {}, 消息ID: {}", destination, message, recordId);
        } catch (Exception e) {
            log.error("发送消息到Redis Stream {} 失败: {}", destination, e.getMessage(), e);
            throw new RuntimeException("消息发送失败", e);
        }
    }

    @Override
    public <T> void send(String destination, T payload) {
        send(destination, DefaultStreamMessage.create(payload));
    }

    @Override
    public <T> void send(String destination, T payload, Map<String, Object> headers) {
        send(destination, DefaultStreamMessage.create(payload, headers));
    }
    
    /**
     * 确保Stream存在
     * 
     * @param destination Stream名称
     */
    private void ensureStreamExists(String destination) {
        // 尝试获取Stream信息，如果不存在则创建
        try {
            StreamInfo.XInfoStream info = redisTemplate.opsForStream().info(destination);
            // Stream存在，无需创建
        } catch (Exception e) {
            // Stream不存在，发送一个虚拟消息创建它，然后删除该消息
            Map<String, String> initMessage = new HashMap<>();
            initMessage.put("init", "true");
            
            RecordId recordId = redisTemplate.opsForStream().add(destination, initMessage);
            redisTemplate.opsForStream().delete(destination, recordId);
            
            log.info("创建Redis Stream: {}", destination);
        }
    }
} 