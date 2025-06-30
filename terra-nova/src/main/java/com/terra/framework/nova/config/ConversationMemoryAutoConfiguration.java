package com.terra.framework.nova.config;

import com.terra.framework.nova.memory.ConversationMemory;
import com.terra.framework.nova.memory.InMemoryConversationMemory;
import com.terra.framework.nova.properties.TerraAiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnProperty(prefix = "terra.ai", name = "enabled", havingValue = "true")
public class ConversationMemoryAutoConfiguration {

    private final TerraAiProperties properties;

    public ConversationMemoryAutoConfiguration(TerraAiProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnProperty(prefix = "terra.ai.memory", name = "type", havingValue = "in-memory", matchIfMissing = true)
    public ConversationMemory inMemoryConversationMemory() {
        return new InMemoryConversationMemory(properties.getMemory());
    }
}
