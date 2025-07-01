package com.terra.framework.autoconfigure.nova.config;

import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import com.terra.framework.nova.memory.ConversationMemory;
import com.terra.framework.nova.template.ConversationOperations;
import com.terra.framework.nova.template.ConversationTemplate;
import com.terra.framework.nova.template.RagOperations;
import com.terra.framework.nova.template.RagTemplate;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author AI
 */
@AutoConfiguration
@ConditionalOnClass(TerraAiProperties.class)
@EnableConfigurationProperties(TerraAiProperties.class)
@Import({
    DeepSeekAutoConfiguration.class,
    VectorStoreAutoConfiguration.class,
    ConversationMemoryAutoConfiguration.class
})
public class TerraAiAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(RagOperations.class)
    public RagOperations ragTemplate(EmbeddingModel embeddingModel, ChatModel chatModel, VectorStore vectorStore) {
        return new RagTemplate(embeddingModel, chatModel, vectorStore);
    }

    @Bean
    @ConditionalOnMissingBean(ConversationOperations.class)
    public ConversationOperations conversationTemplate(ChatModel chatModel, ConversationMemory conversationMemory) {
        return new ConversationTemplate(chatModel, conversationMemory);
    }
}
