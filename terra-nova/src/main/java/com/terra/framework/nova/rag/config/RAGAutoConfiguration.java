package com.terra.framework.nova.rag.config;

import com.terra.framework.nova.llm.config.AIServiceAutoConfiguration;
import com.terra.framework.nova.llm.model.AIModelManager;
import com.terra.framework.nova.rag.context.ContextBuilder;
import com.terra.framework.nova.rag.context.impl.DefaultContextBuilder;
import com.terra.framework.nova.rag.document.DocumentSplitter;
import com.terra.framework.nova.rag.document.impl.RecursiveCharacterSplitter;
import com.terra.framework.nova.rag.embedding.EmbeddingService;
import com.terra.framework.nova.rag.embedding.impl.DefaultEmbeddingService;
import com.terra.framework.nova.rag.properties.EmbeddingProperties;
import com.terra.framework.nova.rag.properties.RAGProperties;
import com.terra.framework.nova.rag.retrieval.Retriever;
import com.terra.framework.nova.rag.retrieval.impl.DefaultRetriever;
import com.terra.framework.nova.rag.service.RAGService;
import com.terra.framework.nova.rag.service.impl.DefaultRAGService;
import com.terra.framework.nova.rag.storage.VectorStore;
import com.terra.framework.nova.rag.storage.impl.InMemoryVectorStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * RAG自动配置类
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Slf4j
@EnableConfigurationProperties({RAGProperties.class, EmbeddingProperties.class})
@ConditionalOnProperty(prefix = "terra.nova.rag", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(AIServiceAutoConfiguration.class)
public class RAGAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public DocumentSplitter documentSplitter(RAGProperties properties) {
        RAGProperties.Splitting splittingConfig = properties.getSplitting();

        if ("character".equalsIgnoreCase(splittingConfig.getSplitter())) {
            return new RecursiveCharacterSplitter(
                splittingConfig.getChunkSize(),
                splittingConfig.getOverlap()
            );
        }

        // 默认使用字符分割器
        log.info("使用默认字符分割器");
        return new RecursiveCharacterSplitter(1000, 200);
    }

    @Bean
    @ConditionalOnMissingBean
    public VectorStore vectorStore(RAGProperties properties) {
        RAGProperties.VectorStore config = properties.getVectorStore();

        if ("in-memory".equalsIgnoreCase(config.getType())) {
            log.info("初始化内存向量存储");
            return new InMemoryVectorStore();
        }

        // 如果需要其他类型的向量存储，可以在这里添加
        // 例如: Milvus, Qdrant, Weaviate等

        // 默认使用内存存储
        log.info("使用默认内存向量存储");
        return new InMemoryVectorStore();
    }

    @Bean
    @ConditionalOnMissingBean
    public EmbeddingService embeddingService(AIModelManager modelManager, EmbeddingProperties properties) {
        log.info("初始化嵌入服务, 使用模型: {}", properties.getModelId());
        return new DefaultEmbeddingService(modelManager, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public Retriever retriever(VectorStore vectorStore, EmbeddingService embeddingService, RAGProperties properties) {
        log.info("初始化检索器, 默认返回结果数量: {}", properties.getRetrieval().getTopK());
        return new DefaultRetriever(vectorStore, embeddingService, properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public ContextBuilder contextBuilder(RAGProperties properties) {
        log.info("初始化上下文构建器");
        return new DefaultContextBuilder(properties);
    }

    @Bean
    @ConditionalOnMissingBean
    public RAGService ragService(
        Retriever retriever,
        EmbeddingService embeddingService,
        DocumentSplitter documentSplitter,
        ContextBuilder contextBuilder,
        RAGProperties properties) {
        log.info("初始化RAG服务");
        return new DefaultRAGService(
            retriever,
            embeddingService,
            documentSplitter,
            contextBuilder,
            properties
        );
    }
}
