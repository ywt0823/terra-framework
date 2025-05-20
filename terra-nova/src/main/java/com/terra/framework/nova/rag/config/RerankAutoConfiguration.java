package com.terra.framework.nova.rag.config;

import com.terra.framework.nova.llm.model.AIModelManager;
import com.terra.framework.nova.rag.properties.RerankProperties;
import com.terra.framework.nova.rag.retrieval.rerank.Reranker;
import com.terra.framework.nova.rag.retrieval.rerank.impl.CrossEncoderReranker;
import com.terra.framework.nova.rag.retrieval.rerank.impl.LLMReranker;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 重排序自动配置类
 *
 * @author Terra Framework Team
 * @date 2025年6月15日
 */
@Slf4j
@EnableConfigurationProperties(RerankProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.rag.rerank", name = "enabled", havingValue = "true")
@AutoConfigureAfter(RAGAutoConfiguration.class)
public class RerankAutoConfiguration {

    /**
     * 配置重排序器
     *
     * @param modelManager AI模型管理器
     * @param properties   重排序配置
     * @return 重排序器
     */
    @Bean
    @ConditionalOnMissingBean
    public Reranker reranker(AIModelManager modelManager, RerankProperties properties) {
        log.info("创建重排序器: {}", properties.getType());

        switch (properties.getType().toLowerCase()) {
            case "llm":
                return new LLMReranker(modelManager, properties);
            case "cross-encoder":
            default:
                return new CrossEncoderReranker(modelManager, properties);
        }
    }
}
