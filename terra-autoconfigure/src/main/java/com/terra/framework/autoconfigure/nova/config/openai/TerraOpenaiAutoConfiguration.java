package com.terra.framework.autoconfigure.nova.config.openai;

import com.terra.framework.autoconfigure.nova.annoation.ConditionalOnModelEnabled;
import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import com.terra.framework.nova.client.openai.OpenAiChatClient;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatAutoConfiguration;
import org.springframework.ai.model.openai.autoconfigure.OpenAiChatProperties;
import org.springframework.ai.model.openai.autoconfigure.OpenAiConnectionProperties;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.redis.RedisVectorStore;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPooled;

import static org.springframework.ai.model.SpringAIModels.OPENAI;

/**
 * Terra OpenAI 自动配置类。
 * <p>
 * 提供 OpenAI 模型相关的 Bean 配置，包括 OpenAiChatClient、RedisVectorStore 等。
 *
 * @author <a href="mailto:love.yu@terra.com">Yu</a>
 * @version 1.0.0
 * @since 1.0.0
 */
@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnClass({OpenAiConnectionProperties.class, OpenAiChatProperties.class})
@AutoConfigureAfter({OpenAiChatAutoConfiguration.class})
@ConditionalOnModelEnabled(OPENAI)
public class TerraOpenaiAutoConfiguration {

    /**
     * 创建 OpenAiChatClient Bean。
     * <p>
     * 当 OpenAiChatModel Bean 存在时，自动创建 OpenAiChatClient 实例。
     *
     * @param openAiChatModel OpenAI 聊天模型实例
     * @return OpenAiChatClient 实例
     */
    @Bean
    @ConditionalOnBean(OpenAiChatModel.class)
    public OpenAiChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return new OpenAiChatClient(openAiChatModel);
    }

    /**
     * 创建 RedisVectorStore Bean。
     * <p>
     * 当 Redis 相关类和 OpenAiEmbeddingModel 可用时，自动创建 RedisVectorStore 实例。
     *
     * @param embeddingModel  OpenAI 嵌入模型实例
     * @param redisProperties Redis 配置属性
     * @return RedisVectorStore 实例
     */
    @Bean
    @ConditionalOnClass({RedisProperties.class, RedisVectorStore.class})
    @ConditionalOnBean(OpenAiEmbeddingModel.class)
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
