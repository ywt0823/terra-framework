package com.terra.framework.nova.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Terra-Nova 配置属性
 * 
 * <p>绑定 application.yml 中 terra.nova 前缀的配置项
 * 
 * @author terra-nova
 * @since 0.0.1
 */
@ConfigurationProperties(prefix = "terra.nova")
public class TerraNovaProperties {

    /**
     * 是否启用 Terra-Nova
     */
    private boolean enabled = true;

    /**
     * Spring AI 集成配置
     */
    @NestedConfigurationProperty
    private SpringAI springAi = new SpringAI();

    /**
     * 模型配置
     */
    @NestedConfigurationProperty
    private Models models = new Models();

    /**
     * 增强功能配置
     */
    @NestedConfigurationProperty
    private Enhancement enhancement = new Enhancement();

    /**
     * RAG 配置
     */
    @NestedConfigurationProperty
    private Rag rag = new Rag();

    /**
     * 模型混合配置
     */
    @NestedConfigurationProperty
    private Blending blending = new Blending();

    /**
     * 向量存储配置
     */
    @NestedConfigurationProperty
    private VectorStores vectorStores = new VectorStores();

    /**
     * 可观测性配置
     */
    @NestedConfigurationProperty
    private Observability observability = new Observability();

    // Getters and Setters
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public SpringAI getSpringAi() {
        return springAi;
    }

    public void setSpringAi(SpringAI springAi) {
        this.springAi = springAi;
    }

    public Models getModels() {
        return models;
    }

    public void setModels(Models models) {
        this.models = models;
    }

    public Enhancement getEnhancement() {
        return enhancement;
    }

    public void setEnhancement(Enhancement enhancement) {
        this.enhancement = enhancement;
    }

    public Rag getRag() {
        return rag;
    }

    public void setRag(Rag rag) {
        this.rag = rag;
    }

    public Blending getBlending() {
        return blending;
    }

    public void setBlending(Blending blending) {
        this.blending = blending;
    }

    public VectorStores getVectorStores() {
        return vectorStores;
    }

    public void setVectorStores(VectorStores vectorStores) {
        this.vectorStores = vectorStores;
    }

    public Observability getObservability() {
        return observability;
    }

    public void setObservability(Observability observability) {
        this.observability = observability;
    }

    /**
     * Spring AI 集成配置
     */
    public static class SpringAI {
        private boolean enabled = true;
        private boolean autoRegisterModels = true;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isAutoRegisterModels() {
            return autoRegisterModels;
        }

        public void setAutoRegisterModels(boolean autoRegisterModels) {
            this.autoRegisterModels = autoRegisterModels;
        }
    }

    /**
     * 模型配置
     */
    public static class Models {
        private String defaultProvider = "openai";
        private Map<String, Provider> providers = new HashMap<>();

        public String getDefaultProvider() {
            return defaultProvider;
        }

        public void setDefaultProvider(String defaultProvider) {
            this.defaultProvider = defaultProvider;
        }

        public Map<String, Provider> getProviders() {
            return providers;
        }

        public void setProviders(Map<String, Provider> providers) {
            this.providers = providers;
        }

        /**
         * 模型提供商配置
         */
        public static class Provider {
            private String apiKey;
            private String baseUrl;
            private List<Model> models;

            public String getApiKey() {
                return apiKey;
            }

            public void setApiKey(String apiKey) {
                this.apiKey = apiKey;
            }

            public String getBaseUrl() {
                return baseUrl;
            }

            public void setBaseUrl(String baseUrl) {
                this.baseUrl = baseUrl;
            }

            public List<Model> getModels() {
                return models;
            }

            public void setModels(List<Model> models) {
                this.models = models;
            }
        }

        /**
         * 模型配置
         */
        public static class Model {
            private String name;
            private ModelType type = ModelType.CHAT;
            private Integer maxTokens = 4096;
            private Double temperature = 0.7;

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public ModelType getType() {
                return type;
            }

            public void setType(ModelType type) {
                this.type = type;
            }

            public Integer getMaxTokens() {
                return maxTokens;
            }

            public void setMaxTokens(Integer maxTokens) {
                this.maxTokens = maxTokens;
            }

            public Double getTemperature() {
                return temperature;
            }

            public void setTemperature(Double temperature) {
                this.temperature = temperature;
            }
        }

        /**
         * 模型类型枚举
         */
        public enum ModelType {
            CHAT, EMBEDDING, IMAGE, AUDIO, MODERATION
        }
    }

    /**
     * 增强功能配置
     */
    public static class Enhancement {
        @NestedConfigurationProperty
        private Cache cache = new Cache();

        @NestedConfigurationProperty
        private Retry retry = new Retry();

        @NestedConfigurationProperty
        private Monitoring monitoring = new Monitoring();

        public Cache getCache() {
            return cache;
        }

        public void setCache(Cache cache) {
            this.cache = cache;
        }

        public Retry getRetry() {
            return retry;
        }

        public void setRetry(Retry retry) {
            this.retry = retry;
        }

        public Monitoring getMonitoring() {
            return monitoring;
        }

        public void setMonitoring(Monitoring monitoring) {
            this.monitoring = monitoring;
        }

        /**
         * 缓存配置
         */
        public static class Cache {
            private boolean enabled = true;
            private Duration ttl = Duration.ofHours(1);
            private double similarityThreshold = 0.95;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public Duration getTtl() {
                return ttl;
            }

            public void setTtl(Duration ttl) {
                this.ttl = ttl;
            }

            public double getSimilarityThreshold() {
                return similarityThreshold;
            }

            public void setSimilarityThreshold(double similarityThreshold) {
                this.similarityThreshold = similarityThreshold;
            }
        }

        /**
         * 重试配置
         */
        public static class Retry {
            private boolean enabled = true;
            private int maxAttempts = 3;
            @NestedConfigurationProperty
            private Backoff backoff = new Backoff();

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public int getMaxAttempts() {
                return maxAttempts;
            }

            public void setMaxAttempts(int maxAttempts) {
                this.maxAttempts = maxAttempts;
            }

            public Backoff getBackoff() {
                return backoff;
            }

            public void setBackoff(Backoff backoff) {
                this.backoff = backoff;
            }

            /**
             * 退避策略配置
             */
            public static class Backoff {
                private Duration initialInterval = Duration.ofSeconds(1);
                private double multiplier = 2.0;
                private Duration maxInterval = Duration.ofSeconds(10);

                public Duration getInitialInterval() {
                    return initialInterval;
                }

                public void setInitialInterval(Duration initialInterval) {
                    this.initialInterval = initialInterval;
                }

                public double getMultiplier() {
                    return multiplier;
                }

                public void setMultiplier(double multiplier) {
                    this.multiplier = multiplier;
                }

                public Duration getMaxInterval() {
                    return maxInterval;
                }

                public void setMaxInterval(Duration maxInterval) {
                    this.maxInterval = maxInterval;
                }
            }
        }

        /**
         * 监控配置
         */
        public static class Monitoring {
            private boolean enabled = true;
            private boolean metricsEnabled = true;
            private boolean tracingEnabled = true;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public boolean isMetricsEnabled() {
                return metricsEnabled;
            }

            public void setMetricsEnabled(boolean metricsEnabled) {
                this.metricsEnabled = metricsEnabled;
            }

            public boolean isTracingEnabled() {
                return tracingEnabled;
            }

            public void setTracingEnabled(boolean tracingEnabled) {
                this.tracingEnabled = tracingEnabled;
            }
        }
    }

    /**
     * RAG 配置
     */
    public static class Rag {
        private boolean enabled = false;
        @NestedConfigurationProperty
        private VectorStore vectorStore = new VectorStore();
        @NestedConfigurationProperty
        private Document document = new Document();

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public VectorStore getVectorStore() {
            return vectorStore;
        }

        public void setVectorStore(VectorStore vectorStore) {
            this.vectorStore = vectorStore;
        }

        public Document getDocument() {
            return document;
        }

        public void setDocument(Document document) {
            this.document = document;
        }

        /**
         * 向量存储配置
         */
        public static class VectorStore {
            private String type = "redis";
            private double similarityThreshold = 0.8;
            private int topK = 5;

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }

            public double getSimilarityThreshold() {
                return similarityThreshold;
            }

            public void setSimilarityThreshold(double similarityThreshold) {
                this.similarityThreshold = similarityThreshold;
            }

            public int getTopK() {
                return topK;
            }

            public void setTopK(int topK) {
                this.topK = topK;
            }
        }

        /**
         * 文档配置
         */
        public static class Document {
            private int chunkSize = 1000;
            private int chunkOverlap = 200;

            public int getChunkSize() {
                return chunkSize;
            }

            public void setChunkSize(int chunkSize) {
                this.chunkSize = chunkSize;
            }

            public int getChunkOverlap() {
                return chunkOverlap;
            }

            public void setChunkOverlap(int chunkOverlap) {
                this.chunkOverlap = chunkOverlap;
            }
        }
    }

    /**
     * 模型混合配置
     */
    public static class Blending {
        private boolean enabled = false;
        private List<Strategy> strategies;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public List<Strategy> getStrategies() {
            return strategies;
        }

        public void setStrategies(List<Strategy> strategies) {
            this.strategies = strategies;
        }

        /**
         * 混合策略配置
         */
        public static class Strategy {
            private String name;
            private List<String> models;
            private MergeStrategy mergeStrategy = MergeStrategy.BEST_QUALITY;
            private Duration timeout = Duration.ofSeconds(30);

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public List<String> getModels() {
                return models;
            }

            public void setModels(List<String> models) {
                this.models = models;
            }

            public MergeStrategy getMergeStrategy() {
                return mergeStrategy;
            }

            public void setMergeStrategy(MergeStrategy mergeStrategy) {
                this.mergeStrategy = mergeStrategy;
            }

            public Duration getTimeout() {
                return timeout;
            }

            public void setTimeout(Duration timeout) {
                this.timeout = timeout;
            }
        }

        /**
         * 合并策略枚举
         */
        public enum MergeStrategy {
            BEST_QUALITY, FASTEST_RESPONSE, WEIGHTED_AVERAGE, CONSENSUS
        }
    }

    /**
     * 向量存储配置
     */
    public static class VectorStores {
        @NestedConfigurationProperty
        private Redis redis = new Redis();
        @NestedConfigurationProperty
        private PostgreSQL postgresql = new PostgreSQL();

        public Redis getRedis() {
            return redis;
        }

        public void setRedis(Redis redis) {
            this.redis = redis;
        }

        public PostgreSQL getPostgresql() {
            return postgresql;
        }

        public void setPostgresql(PostgreSQL postgresql) {
            this.postgresql = postgresql;
        }

        /**
         * Redis 向量存储配置
         */
        public static class Redis {
            private String host = "localhost";
            private int port = 6379;
            private String indexName = "terra-vectors";
            private String distanceMetric = "COSINE";

            public String getHost() {
                return host;
            }

            public void setHost(String host) {
                this.host = host;
            }

            public int getPort() {
                return port;
            }

            public void setPort(int port) {
                this.port = port;
            }

            public String getIndexName() {
                return indexName;
            }

            public void setIndexName(String indexName) {
                this.indexName = indexName;
            }

            public String getDistanceMetric() {
                return distanceMetric;
            }

            public void setDistanceMetric(String distanceMetric) {
                this.distanceMetric = distanceMetric;
            }
        }

        /**
         * PostgreSQL 向量存储配置
         */
        public static class PostgreSQL {
            private String url = "jdbc:postgresql://localhost:5432/vectordb";
            private String tableName = "vector_store";
            private int dimensions = 1536;

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            public String getTableName() {
                return tableName;
            }

            public void setTableName(String tableName) {
                this.tableName = tableName;
            }

            public int getDimensions() {
                return dimensions;
            }

            public void setDimensions(int dimensions) {
                this.dimensions = dimensions;
            }
        }
    }

    /**
     * 可观测性配置
     */
    public static class Observability {
        @NestedConfigurationProperty
        private Metrics metrics = new Metrics();
        @NestedConfigurationProperty
        private Tracing tracing = new Tracing();
        @NestedConfigurationProperty
        private Logging logging = new Logging();

        public Metrics getMetrics() {
            return metrics;
        }

        public void setMetrics(Metrics metrics) {
            this.metrics = metrics;
        }

        public Tracing getTracing() {
            return tracing;
        }

        public void setTracing(Tracing tracing) {
            this.tracing = tracing;
        }

        public Logging getLogging() {
            return logging;
        }

        public void setLogging(Logging logging) {
            this.logging = logging;
        }

        /**
         * 指标配置
         */
        public static class Metrics {
            private boolean enabled = true;
            @NestedConfigurationProperty
            private Export export = new Export();

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public Export getExport() {
                return export;
            }

            public void setExport(Export export) {
                this.export = export;
            }

            /**
             * 指标导出配置
             */
            public static class Export {
                private boolean prometheus = true;
                private boolean cloudwatch = false;

                public boolean isPrometheus() {
                    return prometheus;
                }

                public void setPrometheus(boolean prometheus) {
                    this.prometheus = prometheus;
                }

                public boolean isCloudwatch() {
                    return cloudwatch;
                }

                public void setCloudwatch(boolean cloudwatch) {
                    this.cloudwatch = cloudwatch;
                }
            }
        }

        /**
         * 追踪配置
         */
        public static class Tracing {
            private boolean enabled = true;
            private double samplingRate = 0.1;

            public boolean isEnabled() {
                return enabled;
            }

            public void setEnabled(boolean enabled) {
                this.enabled = enabled;
            }

            public double getSamplingRate() {
                return samplingRate;
            }

            public void setSamplingRate(double samplingRate) {
                this.samplingRate = samplingRate;
            }
        }

        /**
         * 日志配置
         */
        public static class Logging {
            private String level = "INFO";
            private boolean includeRequestResponse = false;

            public String getLevel() {
                return level;
            }

            public void setLevel(String level) {
                this.level = level;
            }

            public boolean isIncludeRequestResponse() {
                return includeRequestResponse;
            }

            public void setIncludeRequestResponse(boolean includeRequestResponse) {
                this.includeRequestResponse = includeRequestResponse;
            }
        }
    }
} 