package com.terra.framework.stream.redis;

import com.terra.framework.stream.config.StreamProperties;
import com.terra.framework.stream.core.MessageConsumer;
import com.terra.framework.stream.core.MessageProducer;
import com.terra.framework.stream.core.MessageQueue;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Redis Stream消息队列实现
 * 
 * @author terra
 */
@Slf4j
public class RedisStreamMessageQueue implements MessageQueue {

    private final StringRedisTemplate redisTemplate;
    private final StreamProperties.Redis properties;

    public RedisStreamMessageQueue(RedisConnectionFactory connectionFactory, 
                                  StreamProperties.Redis properties) {
        this.redisTemplate = new StringRedisTemplate(connectionFactory);
        this.properties = properties;
    }

    @Override
    public MessageProducer createProducer() {
        return new RedisStreamMessageProducer(redisTemplate, properties);
    }

    @Override
    public MessageConsumer createConsumer(String groupId) {
        return new RedisStreamMessageConsumer(redisTemplate, properties, groupId);
    }

    @Override
    public void createQueue(String name) {
        try {
            // 尝试获取Stream信息，如果能获取到，说明已存在
            try {
                redisTemplate.opsForStream().info(name);
                log.debug("Redis Stream已存在: {}", name);
            } catch (Exception e) {
                // Stream不存在，创建一个初始化消息
                log.info("创建Redis Stream: {}", name);
                Map<String, String> initMessage = new HashMap<>();
                initMessage.put("init", "true");
                
                RecordId recordId = redisTemplate.opsForStream().add(name, initMessage);
                // 可选：删除初始化消息
                redisTemplate.opsForStream().delete(name, recordId);
            }
        } catch (Exception e) {
            log.error("创建Redis Stream失败: {}", name, e);
            throw new RuntimeException("无法创建Stream", e);
        }
    }

    @Override
    public void deleteQueue(String name) {
        try {
            redisTemplate.delete(name);
            log.info("删除Redis Stream: {}", name);
        } catch (Exception e) {
            log.error("删除Redis Stream失败: {}", name, e);
            throw new RuntimeException("无法删除Stream", e);
        }
    }

    @Override
    public List<String> listQueues() {
        // 注意：Redis没有直接提供列出所有Stream的API
        // 这里需要基于项目具体需求实现
        // 可以通过使用KEYS pattern或SCAN命令结合pattern来查找所有stream
        // 在生产环境中，应避免使用KEYS命令，因为它会阻塞Redis
        log.warn("listQueues方法对Redis Stream尚未完全实现");
        return new ArrayList<>();
    }
} 