package com.terra.framework.autoconfigure.strata.config.redis;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.terra.framework.autoconfigure.strata.config.redis.lock.IRedissonLock;
import com.terra.framework.strata.helper.RedisKeyHelper;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.util.StringUtils;

/**
 * @author ywt
 * @description
 * @date 2021年08月09日 15:41
 */
@Slf4j
@ConditionalOnProperty(prefix = "spring.data.redis.lettuce", name = "pool.enabled", havingValue = "true")
public class TerraRedisAutoConfiguration {

    @Bean
    @ConfigurationProperties(prefix = "spring.data.redis")
    public RedisProperties dataSource() {
        return new RedisProperties();
    }

    @Bean
    @ConditionalOnMissingBean
    public RedisKeyHelper redisKeyHelper(ApplicationContext applicationContext) {
        return new RedisKeyHelper(applicationContext.getEnvironment(), applicationContext.getApplicationName());
    }

    private static ObjectMapper createObjectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.activateDefaultTyping(LaissezFaireSubTypeValidator.instance, ObjectMapper.DefaultTyping.NON_FINAL);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        objectMapper.registerModule(new JavaTimeModule());
        return objectMapper;
    }

    @Configuration
    @ConditionalOnClass(LettuceConnectionFactory.class)
    protected static class SpringDataRedisConfiguration {

        @Bean
        @ConditionalOnMissingBean(RedisConnectionFactory.class)
        public LettuceConnectionFactory redisConnectionFactory(RedisProperties properties) {
            log.info("Creating LettuceConnectionFactory with properties: {}", properties);
            RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
            redisStandaloneConfiguration.setHostName(properties.getHost());
            redisStandaloneConfiguration.setPort(properties.getPort());
            redisStandaloneConfiguration.setDatabase(properties.getDatabase());
            if (StringUtils.hasText(properties.getUsername())) {
                redisStandaloneConfiguration.setUsername(properties.getUsername());
            }
            if (StringUtils.hasText(properties.getPassword())) {
                redisStandaloneConfiguration.setPassword(RedisPassword.of(properties.getPassword()));
            }
            return new LettuceConnectionFactory(redisStandaloneConfiguration);
        }

        @Bean("terra-redisTemplate")
        @ConditionalOnMissingBean(name = "redisTemplate")
        public StringRedisTemplate terraRedisTemplate(RedisConnectionFactory redisConnectionFactory) {
            StringRedisTemplate redisTemplate = new StringRedisTemplate();
            redisTemplate.setConnectionFactory(redisConnectionFactory);

            ObjectMapper objectMapper = createObjectMapper();
            Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(objectMapper, Object.class);
            StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

            redisTemplate.setKeySerializer(stringRedisSerializer);
            redisTemplate.setHashKeySerializer(stringRedisSerializer);
            redisTemplate.setValueSerializer(jackson2JsonRedisSerializer);
            redisTemplate.setHashValueSerializer(jackson2JsonRedisSerializer);
            redisTemplate.afterPropertiesSet();

            return redisTemplate;
        }
    }

    @Configuration
    @ConditionalOnClass(RedissonClient.class)
    protected static class RedissonConfiguration {
        @Bean(destroyMethod = "shutdown")
        @ConditionalOnMissingBean
        public RedissonClient redissonClient(RedisProperties properties) {
            Config config = new Config();
            String address = "redis://" + properties.getHost() + ":" + properties.getPort();
            config.useSingleServer()
                .setAddress(address)
                .setUsername(properties.getUsername())
                .setPassword(properties.getPassword())
                .setDatabase(properties.getDatabase());
            return Redisson.create(config);
        }

        @Bean
        @ConditionalOnMissingBean
        public RBlockingQueue<String> blockingQueue(RedissonClient redissonClient) {
            return redissonClient.getBlockingQueue("terra-delayedQueue");
        }

        @Bean
        @ConditionalOnMissingBean
        public RDelayedQueue<String> delayedQueue(RBlockingQueue<String> blockingQueue, RedissonClient redissonClient) {
            return redissonClient.getDelayedQueue(blockingQueue);
        }

        @Bean
        @ConditionalOnMissingBean
        public IRedissonLock redissonLock(RedissonClient redissonClient) {
            return new IRedissonLock(redissonClient);
        }
    }
}
