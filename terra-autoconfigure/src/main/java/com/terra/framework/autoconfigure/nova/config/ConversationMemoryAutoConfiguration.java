package com.terra.framework.autoconfigure.nova.config;

import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import com.terra.framework.nova.memory.ConversationMemory;
import com.terra.framework.nova.memory.InMemoryConversationMemory;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration
@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnProperty(prefix = "terra.ai.memory", name = "type", havingValue = "in-memory", matchIfMissing = true)
@AutoConfigureAfter(DeepSeekAutoConfiguration.class)
public class ConversationMemoryAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean
    public ConversationMemory inMemoryConversationMemory(TerraAiProperties properties) {
        return new InMemoryConversationMemory(properties.getMemory().getMaxHistory());
    }
}
