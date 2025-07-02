package com.terra.framework.autoconfigure.nova.config.openai;

import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import com.terra.framework.nova.template.ConversationTemplate;
import com.terra.framework.nova.template.RagOperations;
import com.terra.framework.nova.template.RagTemplate;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiConnectionProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPooled;

import static org.springframework.ai.model.SpringAIModels.OPENAI;

@ConditionalOnProperty(prefix = "spring.ai.model", name = "modelType", havingValue = OPENAI)
@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnClass({OpenAiConnectionProperties.class, OpenAiChatProperties.class})
@AutoConfigureAfter({OpenAiChatAutoConfiguration.class})
public class TerraOpenaiAutoConfiguration {


    @Bean
    @ConditionalOnMissingBean(RagOperations.class)
    public RagTemplate ragTemplate(OpenAiChatModel chatModel,
                                   VectorStore vectorStore) {
        return new RagTemplate(chatModel, vectorStore);
    }

    @Bean
    @ConditionalOnMissingBean(ConversationTemplate.class)
    public ConversationTemplate conversationTemplate(OpenAiChatModel chatModel) {
        return new ConversationTemplate(chatModel,
            MessageWindowChatMemory.builder()
                .chatMemoryRepository(new InMemoryChatMemoryRepository())
                .maxMessages(20)
                .build());
    }


    @Bean
    @ConditionalOnClass({RedisProperties.class, RedisVectorStore.class})
    public RedisVectorStore redisVectorStore(OpenAiEmbeddingModel embeddingModel, RedisProperties redisProperties) {
        return RedisVectorStore.builder(new JedisPooled("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort()), embeddingModel)
            .vectorAlgorithm(RedisVectorStore.Algorithm.HSNW)
            .contentFieldName("terra-content")
            .embeddingFieldName("terra-embedding")
            .indexName("terra-index")
            .prefix("terra")
            .metadataFields(RedisVectorStore.MetadataField.text("terra-text"),
                RedisVectorStore.MetadataField.numeric("terra-number"),
                RedisVectorStore.MetadataField.tag("terra-tag"))
            .initializeSchema(false)
            .build();
    }


}
