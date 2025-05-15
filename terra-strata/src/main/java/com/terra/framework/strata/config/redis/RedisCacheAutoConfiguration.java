package com.terra.framework.strata.config.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terra.framework.geyser.config.CacheAutoConfiguration;
import com.terra.framework.geyser.factory.CacheFactory;
import com.terra.framework.geyser.options.CacheOperation;
import com.terra.framework.strata.helper.RedisKeyHelper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Redis缓存自动配置
 *
 * @author terra
 */
@Slf4j
@ConditionalOnClass(CacheAutoConfiguration.class)
@AutoConfigureAfter(TerraRedisAutoConfiguration.class)
@AutoConfigureBefore(CacheAutoConfiguration.class)
public class RedisCacheAutoConfiguration {

    @Bean
    @ConditionalOnBean(StringRedisTemplate.class)
    public CacheFactory redisCacheFactory(
            @Qualifier("terra-redisTemplate") StringRedisTemplate redisTemplate,
            RedisKeyHelper redisKeyHelper,
            ObjectMapper objectMapper) {
        return new RedisCacheFactory(redisTemplate, redisKeyHelper, objectMapper);
    }

    /**
     * Redis缓存工厂实现
     */
    @Slf4j
    public static class RedisCacheFactory implements CacheFactory {

        private final StringRedisTemplate redisTemplate;
        private final RedisKeyHelper redisKeyHelper;
        private final ObjectMapper objectMapper;
        private final Map<String, CacheOperation<?, ?>> caches = new ConcurrentHashMap<>();

        public RedisCacheFactory(StringRedisTemplate redisTemplate,
                                 RedisKeyHelper redisKeyHelper,
                                 ObjectMapper objectMapper) {
            this.redisTemplate = redisTemplate;
            this.redisKeyHelper = redisKeyHelper;
            this.objectMapper = objectMapper;
        }

        @Override
        @SuppressWarnings("unchecked")
        public <K, V> CacheOperation<K, V> createAccessCache(String name, int maxSize, long expireAfterAccess, TimeUnit timeUnit) {
            return (CacheOperation<K, V>) caches.computeIfAbsent(name, n -> {
                log.info("创建Redis缓存[{}]，访问后过期时间：{} {}", name, expireAfterAccess, timeUnit);
                return new RedisCacheOperation<>(name, redisTemplate, redisKeyHelper, objectMapper,
                        expireAfterAccess, timeUnit, true);
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public <K, V> CacheOperation<K, V> createWriteCache(String name, int maxSize, long expireAfterWrite, TimeUnit timeUnit) {
            return (CacheOperation<K, V>) caches.computeIfAbsent(name, n -> {
                log.info("创建Redis缓存[{}]，写入后过期时间：{} {}", name, expireAfterWrite, timeUnit);
                return new RedisCacheOperation<>(name, redisTemplate, redisKeyHelper, objectMapper,
                        expireAfterWrite, timeUnit, false);
            });
        }

        @Override
        @SuppressWarnings("unchecked")
        public <K, V> CacheOperation<K, V> getCache(String name) {
            return (CacheOperation<K, V>) caches.get(name);
        }
    }

    /**
     * Redis缓存操作实现
     */
    @Slf4j
    public static class RedisCacheOperation<K, V> implements CacheOperation<K, V> {
        private final String name;
        private final StringRedisTemplate redisTemplate;
        private final RedisKeyHelper redisKeyHelper;
        private final ObjectMapper objectMapper;
        private final long expireTime;
        private final TimeUnit timeUnit;
        private final boolean isAccessExpire;

        // 简单的统计信息
        private long hitCount = 0;
        private long missCount = 0;
        private long requestCount = 0;

        public RedisCacheOperation(String name, StringRedisTemplate redisTemplate,
                                   RedisKeyHelper redisKeyHelper, ObjectMapper objectMapper,
                                   long expireTime, TimeUnit timeUnit, boolean isAccessExpire) {
            this.name = name;
            this.redisTemplate = redisTemplate;
            this.redisKeyHelper = redisKeyHelper;
            this.objectMapper = objectMapper;
            this.expireTime = expireTime;
            this.timeUnit = timeUnit;
            this.isAccessExpire = isAccessExpire;
        }

        @Override
        public V get(K key, Callable<V> callable) {
            requestCount++;
            String redisKey = generateRedisKey(key);

            try {
                String value = redisTemplate.opsForValue().get(redisKey);

                if (value != null) {
                    hitCount++;

                    // 如果是访问后过期，需要重新设置过期时间
                    if (isAccessExpire) {
                        redisTemplate.expire(redisKey, expireTime, timeUnit);
                    }

                    return deserialize(value);
                }

                missCount++;
                // 缓存未命中，调用callable获取值
                V newValue = callable.call();
                if (newValue != null) {
                    String serializedValue = serialize(newValue);
                    redisTemplate.opsForValue().set(redisKey, serializedValue, expireTime, timeUnit);
                }
                return newValue;

            } catch (Exception e) {
                log.error("从Redis缓存[{}]获取键[{}]值失败", name, key, e);
                try {
                    // 尝试直接调用callable
                    return callable.call();
                } catch (Exception ex) {
                    log.error("调用callable获取值失败", ex);
                    return null;
                }
            }
        }

        @Override
        public V getIfPresent(K key) {
            requestCount++;
            String redisKey = generateRedisKey(key);

            try {
                String value = redisTemplate.opsForValue().get(redisKey);
                if (value != null) {
                    hitCount++;

                    // 如果是访问后过期，需要重新设置过期时间
                    if (isAccessExpire) {
                        redisTemplate.expire(redisKey, expireTime, timeUnit);
                    }

                    return deserialize(value);
                }

                missCount++;
                return null;
            } catch (Exception e) {
                log.error("从Redis缓存[{}]获取键[{}]值失败", name, key, e);
                return null;
            }
        }

        @Override
        public void put(K key, V value) {
            if (value == null) {
                return;
            }

            String redisKey = generateRedisKey(key);
            try {
                String serializedValue = serialize(value);
                redisTemplate.opsForValue().set(redisKey, serializedValue, expireTime, timeUnit);
            } catch (Exception e) {
                log.error("向Redis缓存[{}]放入键[{}]值失败", name, key, e);
            }
        }

        @Override
        public void putAll(Map<K, V> map) {
            if (map == null || map.isEmpty()) {
                return;
            }

            Map<String, String> redisMap = new HashMap<>(map.size());
            try {
                for (Map.Entry<K, V> entry : map.entrySet()) {
                    if (entry.getValue() != null) {
                        String redisKey = generateRedisKey(entry.getKey());
                        String serializedValue = serialize(entry.getValue());
                        redisMap.put(redisKey, serializedValue);
                    }
                }

                // 批量设置值
                redisTemplate.opsForValue().multiSet(redisMap);

                // 设置过期时间
                for (String key : redisMap.keySet()) {
                    redisTemplate.expire(key, expireTime, timeUnit);
                }
            } catch (Exception e) {
                log.error("向Redis缓存[{}]批量放入值失败", name, e);
            }
        }

        @Override
        public void invalidate(K key) {
            String redisKey = generateRedisKey(key);
            try {
                redisTemplate.delete(redisKey);
            } catch (Exception e) {
                log.error("从Redis缓存[{}]删除键[{}]失败", name, key, e);
            }
        }

        @Override
        public void invalidateAll(List<K> keys) {
            if (keys == null || keys.isEmpty()) {
                return;
            }

            List<String> redisKeys = new ArrayList<>(keys.size());
            try {
                for (K key : keys) {
                    redisKeys.add(generateRedisKey(key));
                }
                redisTemplate.delete(redisKeys);
            } catch (Exception e) {
                log.error("从Redis缓存[{}]批量删除键失败", name, e);
            }
        }

        @Override
        public void invalidateAll() {
            try {
                // 获取所有匹配的键并删除
                String pattern = redisKeyHelper.getKeyName(name, "*");
                redisTemplate.delete(redisTemplate.keys(pattern));
            } catch (Exception e) {
                log.error("清空Redis缓存[{}]失败", name, e);
            }
        }

        @Override
        public String getStats() {
            double hitRate = requestCount == 0 ? 0 : (double) hitCount / requestCount * 100;
            return String.format("命中率: %.2f%%, 总请求次数: %d, 总命中次数: %d, 总未命中次数: %d",
                    hitRate, requestCount, hitCount, missCount);
        }

        @Override
        public String getName() {
            return name;
        }

        /**
         * 生成Redis键
         */
        private String generateRedisKey(K key) {
            return redisKeyHelper.getKeyName(name, key.toString());
        }

        /**
         * 序列化对象为字符串
         */
        @SuppressWarnings("unchecked")
        private String serialize(V value) {
            try {
                return objectMapper.writeValueAsString(value);
            } catch (Exception e) {
                log.error("序列化对象失败", e);
                return null;
            }
        }

        /**
         * 反序列化字符串为对象
         */
        @SuppressWarnings("unchecked")
        private V deserialize(String json) {
            try {
                // 理想情况下应该知道具体的类型，这里做了简化处理
                return (V) objectMapper.readValue(json, Object.class);
            } catch (Exception e) {
                log.error("反序列化JSON失败: {}", json, e);
                return null;
            }
        }
    }
}
