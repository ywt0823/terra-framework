package com.terra.framework.nova.config;

import com.terra.framework.nova.properties.TerraAiProperties;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnProperty(prefix = "terra.ai", name = "enabled", havingValue = "true")
public class VectorStoreAutoConfiguration {

    @Configuration(proxyBeanMethods = false)
    @ConditionalOnProperty(prefix = "terra.ai.vector-store", name = "type", havingValue = "in-memory", matchIfMissing = true)
    public static class InMemoryVectorStoreConfiguration {

        @Bean
        public VectorStore simpleVectorStore(EmbeddingModel embeddingModel) {
            return new SimpleVectorStore(embeddingModel);
        }
    }

}
