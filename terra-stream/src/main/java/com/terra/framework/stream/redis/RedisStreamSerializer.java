package com.terra.framework.stream.redis;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;

/**
 * Redis Stream序列化工具类
 * 负责对象与JSON字符串之间的转换
 * 
 * @author terra
 */
@Slf4j
public class RedisStreamSerializer {
    
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    /**
     * 序列化对象为JSON字符串
     *
     * @param object 要序列化的对象
     * @return JSON字符串
     */
    public static String serialize(Object object) {
        if (object == null) {
            return null;
        }
        
        try {
            return objectMapper.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            log.error("序列化对象失败: {}", e.getMessage(), e);
            throw new RuntimeException("序列化失败", e);
        }
    }
    
    /**
     * 反序列化JSON字符串为指定类型对象
     *
     * @param json JSON字符串
     * @param type 目标类型
     * @return 反序列化后的对象
     */
    public static <T> T deserialize(String json, Class<T> type) {
        if (json == null || json.isEmpty()) {
            return null;
        }
        
        try {
            return objectMapper.readValue(json, type);
        } catch (JsonProcessingException e) {
            log.error("反序列化JSON失败: {}", e.getMessage(), e);
            throw new RuntimeException("反序列化失败", e);
        }
    }
} 