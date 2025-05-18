package com.terra.framework.nova.conversation.config;

import com.terra.framework.nova.conversation.properties.ConversationProperties;
import com.terra.framework.nova.conversation.service.ConversationService;
import com.terra.framework.nova.conversation.service.impl.ConversationAwareAIService;
import com.terra.framework.nova.conversation.service.impl.DefaultConversationService;
import com.terra.framework.nova.conversation.storage.ConversationStorage;
import com.terra.framework.nova.conversation.storage.impl.LocalConversationStorage;
import com.terra.framework.nova.llm.config.AIServiceAutoConfiguration;
import com.terra.framework.nova.llm.service.EnhancedAIService;
import com.terra.framework.nova.llm.service.EnhancedDefaultAIService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
@EnableConfigurationProperties(ConversationProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.conversation", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(AIServiceAutoConfiguration.class)
public class ConversationAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ConversationStorage conversationStorage() {
        return new LocalConversationStorage();
    }

    @Bean
    @ConditionalOnMissingBean
    public ConversationService conversationService(ConversationStorage storage) {
        return new DefaultConversationService(storage);
    }

    @Bean
    @Primary
    @ConditionalOnBean(EnhancedDefaultAIService.class)
    public EnhancedAIService conversationAwareAIService(EnhancedDefaultAIService delegate, ConversationService conversationService) {
        return new ConversationAwareAIService(delegate, conversationService);
    }
}
