package com.terra.framework.nova.core.model;

import lombok.Data;

/**
 * 模型装饰器选项
 *
 * @author terra-nova
 */
@Data
public class ModelDecoratorOptions {

    /**
     * 是否启用重试
     */
    private boolean retryEnabled = true;

    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;

    /**
     * 是否启用指标收集
     */
    private boolean metricsEnabled = true;

    /**
     * 是否启用路由
     */
    private boolean routingEnabled = false;

    /**
     * 重试配置
     */
    private RetryConfig retryConfig = RetryConfig.builder().build();

    /**
     * 默认TTL（秒）
     */
    private int defaultCacheTtlSeconds = 3600;

    /**
     * 默认静态实例
     */
    private static final ModelDecoratorOptions DEFAULT = new ModelDecoratorOptions();

    /**
     * 获取默认选项
     *
     * @return 默认选项
     */
    public static ModelDecoratorOptions getDefault() {
        return DEFAULT;
    }

    /**
     * 创建一个新的构建器
     *
     * @return 构建器
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 选项构建器
     */
    public static class Builder {
        private final ModelDecoratorOptions options = new ModelDecoratorOptions();

        /**
         * 设置是否启用重试
         *
         * @param enabled 是否启用
         * @return 构建器
         */
        public Builder retryEnabled(boolean enabled) {
            options.setRetryEnabled(enabled);
            return this;
        }

        /**
         * 设置是否启用缓存
         *
         * @param enabled 是否启用
         * @return 构建器
         */
        public Builder cacheEnabled(boolean enabled) {
            options.setCacheEnabled(enabled);
            return this;
        }

        /**
         * 设置是否启用指标收集
         *
         * @param enabled 是否启用
         * @return 构建器
         */
        public Builder metricsEnabled(boolean enabled) {
            options.setMetricsEnabled(enabled);
            return this;
        }

        /**
         * 设置是否启用路由
         *
         * @param enabled 是否启用
         * @return 构建器
         */
        public Builder routingEnabled(boolean enabled) {
            options.setRoutingEnabled(enabled);
            return this;
        }

        /**
         * 设置重试配置
         *
         * @param config 重试配置
         * @return 构建器
         */
        public Builder retryConfig(RetryConfig config) {
            options.setRetryConfig(config);
            return this;
        }

        /**
         * 设置默认缓存TTL
         *
         * @param ttlSeconds TTL（秒）
         * @return 构建器
         */
        public Builder defaultCacheTtlSeconds(int ttlSeconds) {
            options.setDefaultCacheTtlSeconds(ttlSeconds);
            return this;
        }

        /**
         * 构建选项
         *
         * @return 装饰器选项
         */
        public ModelDecoratorOptions build() {
            return options;
        }
    }
}
