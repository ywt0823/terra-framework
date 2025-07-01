package com.terra.framework.autoconfigure.nova.config;

import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnProperty(prefix = "terra.ai.vector-store", name = "type", havingValue = "in-memory", matchIfMissing = true)
@AutoConfigureAfter(DeepSeekAutoConfiguration.class)
public class VectorStoreAutoConfiguration {

    @Bean
    public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }

}
