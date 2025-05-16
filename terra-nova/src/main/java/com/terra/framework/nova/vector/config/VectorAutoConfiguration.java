package com.terra.framework.nova.vector.config;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.model.properties.ModelProperties;
import com.terra.framework.nova.vector.document.DocumentProcessor;
import com.terra.framework.nova.vector.embedding.EmbeddingService;
import com.terra.framework.nova.vector.embedding.OpenAIEmbedding;
import com.terra.framework.nova.vector.properties.VectorProperties;
import com.terra.framework.nova.vector.redis.RedisConfig;
import com.terra.framework.nova.vector.redis.RedisVectorClient;
import com.terra.framework.nova.vector.redis.RedisVectorStore;
import com.terra.framework.nova.vector.search.VectorSearchService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import redis.clients.jedis.JedisPool;

/**
 * 向量存储自动配置类
 *
 * @author terra-nova
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties({VectorProperties.class, ModelProperties.class})
@ConditionalOnProperty(prefix = "terra.nova.vector", name = "enabled", havingValue = "true", matchIfMissing = true)
public class VectorAutoConfiguration {

    /**
     * 配置嵌入服务
     */
    @Bean
    @ConditionalOnMissingBean
    public EmbeddingService embeddingService(VectorProperties vectorProperties,
                                            ModelProperties modelProperties,
                                            HttpClientUtils httpClientUtils) {
        String apiKey = modelProperties.getOpenai().getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("未配置OpenAI API密钥，将尝试从环境变量获取");
            apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("无法获取OpenAI API密钥，嵌入服务将无法正常工作");
            }
        }

        log.info("初始化OpenAI嵌入服务，模型: {}", vectorProperties.getEmbeddingModel());
        return new OpenAIEmbedding(
                httpClientUtils,
                apiKey,
                vectorProperties.getEmbeddingModel(),
                vectorProperties.getBatchSize()
        );
    }

    /**
     * 配置文档处理器
     */
    @Bean
    @ConditionalOnMissingBean
    public DocumentProcessor documentProcessor(VectorProperties vectorProperties) {
        log.info("初始化文档处理器，块大小: {}，块重叠: {}",
                vectorProperties.getChunkSize(), vectorProperties.getChunkOverlap());
        return new DocumentProcessor(
                vectorProperties.getChunkSize(),
                vectorProperties.getChunkOverlap()
        );
    }

    /**
     * 配置Redis配置
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisConfig redisConfig(VectorProperties vectorProperties) {
        VectorProperties.RedisProperties redisProps = vectorProperties.getRedis();

        RedisConfig redisConfig = new RedisConfig();
        redisConfig.setHost(redisProps.getHost());
        redisConfig.setPort(redisProps.getPort());
        redisConfig.setPassword(redisProps.getPassword());
        redisConfig.setDatabase(redisProps.getDatabase());
        redisConfig.setTimeout(redisProps.getTimeout());
        redisConfig.setMaxTotal(redisProps.getMaxTotal());
        redisConfig.setMaxIdle(redisProps.getMaxIdle());
        redisConfig.setMinIdle(redisProps.getMinIdle());
        redisConfig.setVectorKeyPrefix(redisProps.getVectorKeyPrefix());
        redisConfig.setMetadataKeyPrefix(redisProps.getMetadataKeyPrefix());

        log.info("初始化Redis配置，主机: {}，端口: {}", redisProps.getHost(), redisProps.getPort());
        return redisConfig;
    }

    /**
     * 配置Jedis连接池
     */
    @Bean
    @ConditionalOnMissingBean
    public JedisPool jedisPool(RedisConfig redisConfig) {
        log.info("初始化Jedis连接池");
        return redisConfig.createJedisPool();
    }

    /**
     * 配置Redis向量客户端
     */
    @Bean
    @ConditionalOnMissingBean
    public RedisVectorClient redisVectorClient(JedisPool jedisPool, RedisConfig redisConfig) {
        log.info("初始化Redis向量客户端");
        return new RedisVectorClient(jedisPool, redisConfig);
    }

    /**
     * 配置向量搜索服务
     */
    @Bean
    @ConditionalOnMissingBean
    public VectorSearchService vectorSearchService(RedisVectorClient redisVectorClient,
                                                 EmbeddingService embeddingService,
                                                 DocumentProcessor documentProcessor,
                                                 VectorProperties vectorProperties) {
        log.info("初始化向量搜索服务，相似度阈值: {}", vectorProperties.getSimilarityThreshold());
        return new RedisVectorStore(
                redisVectorClient,
                embeddingService,
                documentProcessor,
                vectorProperties.getSimilarityThreshold()
        );
    }
}
