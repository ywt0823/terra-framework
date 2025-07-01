package com.terra.framework.nova.config;

import com.terra.framework.nova.memory.ConversationMemory;
import com.terra.framework.nova.memory.InMemoryConversationMemory;
import com.terra.framework.nova.memory.RedisConversationMemory;
import com.terra.framework.nova.properties.TerraAiProperties;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@AutoConfiguration
@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnProperty(prefix = "terra.ai", name = "enabled", havingValue = "true")
public class ConversationMemoryAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "terra.ai.memory", name = "type", havingValue = "in-memory", matchIfMissing = true)
    public static class InMemoryConversationMemoryConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ConversationMemory inMemoryConversationMemory(TerraAiProperties properties) {
            return new InMemoryConversationMemory(properties.getMemory());
        }
    }

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnClass(RedissonClient.class)
    @ConditionalOnProperty(prefix = "terra.ai.memory", name = "type", havingValue = "redis")
    public static class RedisConversationMemoryConfiguration {

        @Bean
        @ConditionalOnMissingBean
        public ConversationMemory redisConversationMemory(RedissonClient redissonClient, TerraAiProperties properties) {
            return new RedisConversationMemory(redissonClient);
        }
    }
}
