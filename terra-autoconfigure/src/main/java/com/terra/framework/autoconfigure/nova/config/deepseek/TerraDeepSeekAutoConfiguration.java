package com.terra.framework.autoconfigure.nova.config.deepseek;

import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import com.terra.framework.nova.template.ConversationTemplate;
import com.terra.framework.nova.template.RagOperations;
import com.terra.framework.nova.template.RagTemplate;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatProperties;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekConnectionProperties;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static org.springframework.ai.model.SpringAIModels.DEEPSEEK;

@ConditionalOnProperty(prefix = "spring.ai.model", name = "modelType", havingValue = DEEPSEEK)
@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnClass({DeepSeekConnectionProperties.class, DeepSeekChatProperties.class})
@AutoConfigureBefore({DeepSeekChatAutoConfiguration.class})
public class TerraDeepSeekAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(RagOperations.class)
    public RagTemplate ragTemplate(DeepSeekChatModel chatModel,
                                   VectorStore vectorStore) {
        return new RagTemplate(chatModel, vectorStore);
    }

    @Bean
    @ConditionalOnMissingBean(ConversationTemplate.class)
    public ConversationTemplate conversationTemplate(DeepSeekChatModel chatModel) {
        return new ConversationTemplate(chatModel,
            MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build());
    }


}
