package com.terra.framework.geyser.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.jsontype.impl.LaissezFaireSubTypeValidator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

/**
 * @author ywt
 * @description
 * @date 2021年08月09日 15:41
 */
@EnableConfigurationProperties({RedisProperties.class})
@ConditionalOnClass(LettuceConnectionFactory.class)
@ConditionalOnProperty(prefix = "spring.data.redis.lettuce", name = "pool.enabled", havingValue = "true")
public class TerraRedisAutoConfiguration {

    @Autowired
    private RedisProperties redisProperties;


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
