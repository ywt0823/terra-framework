package com.terra.framework.autoconfigure.nova.config;

import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import com.terra.framework.autoconfigure.strata.config.redis.TerraRedisAutoConfiguration;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.vectorstore.RedisVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@EnableConfigurationProperties({TerraAiProperties.class, RedisProperties.class})
@ConditionalOnProperty(prefix = "terra.ai.vector-store", name = "type", havingValue = "redis")
@AutoConfigureAfter({TerraModelAutoConfiguration.class, TerraRedisAutoConfiguration.class})
@ConditionalOnClass({VectorStore.class, EmbeddingModel.class, RedisProperties.class})
public class TerraRedisVectorStoreAutoConfiguration {

    @Bean
    @Primary
    public RedisVectorStore redisVectorStore(OpenAiEmbeddingModel embeddingModel, RedisProperties redisProperties) {
        RedisVectorStore.RedisVectorStoreConfig redisVectorStoreConfig = RedisVectorStore.RedisVectorStoreConfig.builder()
            .withURI("redis://" + redisProperties.getHost() + ":" + redisProperties.getPort())
            .withVectorAlgorithm(RedisVectorStore.Algorithm.HSNW)
            .withIndexName("terra-vector")
            .withPrefix("terra")
            .withContentFieldName("terra-content")
            .withEmbeddingFieldName("terra-embedding")
            .withMetadataFields(RedisVectorStore.MetadataField.text("terra-text"),
                RedisVectorStore.MetadataField.numeric("terra-number"),
                RedisVectorStore.MetadataField.tag("terra-tag")).build();
        return new RedisVectorStore(redisVectorStoreConfig, embeddingModel, false);
    }

}
