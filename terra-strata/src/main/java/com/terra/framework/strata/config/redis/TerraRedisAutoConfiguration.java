package com.terra.framework.strata.config.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.terra.framework.geyser.factory.CacheFactory;
import com.terra.framework.geyser.options.CacheOperation;
import com.terra.framework.strata.config.redis.lock.IRedissonLock;
import com.terra.framework.strata.helper.RedisKeyHelper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author ywt
 * @description
 * @date 2021年08月09日 15:41
 */
@EnableConfigurationProperties({RedisProperties.class})
@ConditionalOnClass(LettuceConnectionFactory.class)
@ConditionalOnProperty(prefix = "spring.data.redis.lettuce", name = "pool.enabled", havingValue = "true")
@Slf4j
public class TerraRedisAutoConfiguration {

    @Autowired
    private RedisProperties redisProperties;

    @Bean
    public RedisKeyHelper redisKeyHelper(ApplicationContext applicationContext) {
        return new RedisKeyHelper(applicationContext.getEnvironment(), applicationContext.getApplicationName());
    }


    @Bean("terra-redisTemplate")
    public StringRedisTemplate valhallaRedisTemplate() {
        StringRedisTemplate redisTemplate = new StringRedisTemplate();
        LettuceConnectionFactory redisConnectionFactory = getRedisConnectionFactory();
        redisConnectionFactory.start();
        redisTemplate.setConnectionFactory(redisConnectionFactory);
        //Json转换核心对象
        ObjectMapper objectMapper = new ObjectMapper();
        //设置属性可见,设置JSON自动转化
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        //开启默认类型
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        //开启时间转换,不开启报错<Cannot construct instance of `java.time.LocalDateTime`>
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        //开启时间转换,注册JAVA时间转换类
        objectMapper.registerModule(new JavaTimeModule());
        //设置序列化器
        Jackson2JsonRedisSerializer<String> jackson2JsonRedisSerializer = getJackson2JsonRedisSerializer(objectMapper);
        //创建redis中key的序列化器
        StringRedisSerializer stringRedisSerializer = getStringRedisSerializer();
        //设置redis中String类型的key的序列化器
        redisTemplate.setKeySerializer(stringRedisSerializer);
        //设置redis中Hash类型的key的序列化器
        redisTemplate.setHashKeySerializer(stringRedisSerializer);
        //设置redis中String类型value的序列化器
        redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
        //设置Redis中Hash类型的value的序列化器
        redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
        //创建对象后,对其属性做设置
        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
                .setUsername(redisProperties.getUsername())
                .setPassword(redisProperties.getPassword())
                .setConnectionPoolSize(redisProperties.getLettuce().getPool().getMaxIdle())
                .setConnectionMinimumIdleSize(redisProperties.getLettuce().getPool().getMinIdle())
                .setTimeout(1000);
        return Redisson.create(config);
    }

    @Bean
    public RBlockingQueue<String> blockingQueue(RedissonClient redissonClient) {
        //队列名称可以自己定义
        return redissonClient.getBlockingQueue("terra-delayedQueue");
    }

    @Bean
    public RDelayedQueue<String> delayedQueue(RBlockingQueue<String> blockingQueue,
                                              RedissonClient redissonClient) {
        return redissonClient.getDelayedQueue(blockingQueue);
    }


    @Bean
    public IRedissonLock redissonLock(RedissonClient redissonClient) {
        return new IRedissonLock(redissonClient);
    }


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


    private LettuceConnectionFactory getRedisConnectionFactory() {
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setPort(redisProperties.getPort());
        redisStandaloneConfiguration.setDatabase(redisProperties.getDatabase());
        redisStandaloneConfiguration.setHostName(redisProperties.getHost());
        redisStandaloneConfiguration.setPassword(redisProperties.getPassword());
        redisStandaloneConfiguration.setUsername(redisProperties.getUsername());
        return new LettuceConnectionFactory(redisStandaloneConfiguration);
    }

    private Jackson2JsonRedisSerializer<String> getJackson2JsonRedisSerializer(ObjectMapper objectMapper) {
        //设置序列化器
        return new Jackson2JsonRedisSerializer<>(objectMapper, String.class);
    }

    private StringRedisSerializer getStringRedisSerializer() {
        //创建redis中key的序列化器
        return new StringRedisSerializer();
    }


}
