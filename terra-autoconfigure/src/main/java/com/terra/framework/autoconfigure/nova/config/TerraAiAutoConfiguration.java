package com.terra.framework.autoconfigure.nova.config;

import com.terra.framework.nova.memory.ConversationMemory;
import com.terra.framework.nova.template.ConversationOperations;
import com.terra.framework.nova.template.ConversationTemplate;
import com.terra.framework.nova.template.RagOperations;
import com.terra.framework.nova.template.RagTemplate;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author AI
 */
@Import({
    TerraModelAutoConfiguration.class,
    TerraLocalVectorStoreAutoConfiguration.class,
    TerraConversationMemoryAutoConfiguration.class,
    TerraRedisVectorStoreAutoConfiguration.class
})
@ConditionalOnProperty(prefix = "terra.ai", name = "enabled", havingValue = "true")
public class TerraAiAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(RagOperations.class)
    public RagTemplate ragTemplate(OpenAiChatModel chatModel,
                                   VectorStore vectorStore) {
        return new RagTemplate(chatModel, vectorStore);
    }

    @Bean
    @ConditionalOnMissingBean(ConversationOperations.class)
    public ConversationTemplate conversationTemplate(OpenAiChatModel chatModel, ConversationMemory conversationMemory) {
        return new ConversationTemplate(chatModel, conversationMemory);
    }
}
