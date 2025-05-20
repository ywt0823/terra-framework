package com.terra.framework.common.util.httpclient;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * HttpClient配置类
 * 
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Getter
public class HttpClientConfig {
    
    /**
     * 连接超时时间（毫秒）
     */
    private final int connectTimeout;
    
    /**
     * 读取超时时间（毫秒）
     */
    private final int readTimeout;
    
    /**
     * 连接池最大连接数
     */
    private final int maxTotalConnections;
    
    /**
     * 每个路由的最大连接数
     */
    private final int maxConnectionsPerRoute;
    
    /**
     * 是否开启自动重试
     */
    private final boolean retryEnabled;
    
    /**
     * 最大重试次数
     */
    private final int maxRetryCount;
    
    /**
     * 是否验证SSL证书
     */
    private final boolean validateSSLCertificate;
    
    /**
     * 是否在处理响应后关闭连接
     */
    private final boolean closeResponseAfterExecution;
    
    /**
     * 线程池大小
     */
    private final int threadPoolSize;
    
    private HttpClientConfig(Builder builder) {
        this.connectTimeout = builder.connectTimeout;
        this.readTimeout = builder.readTimeout;
        this.maxTotalConnections = builder.maxTotalConnections;
        this.maxConnectionsPerRoute = builder.maxConnectionsPerRoute;
        this.retryEnabled = builder.retryEnabled;
        this.maxRetryCount = builder.maxRetryCount;
        this.validateSSLCertificate = builder.validateSSLCertificate;
        this.closeResponseAfterExecution = builder.closeResponseAfterExecution;
        this.threadPoolSize = builder.threadPoolSize;
    }
    
    /**
     * 创建默认配置
     */
    public static HttpClientConfig defaultConfig() {
        return builder().build();
    }
    
    /**
     * 创建配置构建器
     */
    public static Builder builder() {
        return new Builder();
    }
    
    /**
     * HttpClientConfig构建器
     */
    public static class Builder {
        private int connectTimeout = 5000;
        private int readTimeout = 10000;
        private int maxTotalConnections = 200;
        private int maxConnectionsPerRoute = 50;
        private boolean retryEnabled = true;
        private int maxRetryCount = 3;
        private boolean validateSSLCertificate = true;
        private boolean closeResponseAfterExecution = true;
        private int threadPoolSize = 10;
        
        private Builder() {
        }
        
        public Builder connectTimeout(int connectTimeout) {
            this.connectTimeout = connectTimeout;
            return this;
        }
        
        public Builder readTimeout(int readTimeout) {
            this.readTimeout = readTimeout;
            return this;
        }
        
        public Builder maxTotalConnections(int maxTotalConnections) {
            this.maxTotalConnections = maxTotalConnections;
            return this;
        }
        
        public Builder maxConnectionsPerRoute(int maxConnectionsPerRoute) {
            this.maxConnectionsPerRoute = maxConnectionsPerRoute;
            return this;
        }
        
        public Builder retryEnabled(boolean retryEnabled) {
            this.retryEnabled = retryEnabled;
            return this;
        }
        
        public Builder maxRetryCount(int maxRetryCount) {
            this.maxRetryCount = maxRetryCount;
            return this;
        }
        
        public Builder validateSSLCertificate(boolean validateSSLCertificate) {
            this.validateSSLCertificate = validateSSLCertificate;
            return this;
        }
        
        public Builder closeResponseAfterExecution(boolean closeResponseAfterExecution) {
            this.closeResponseAfterExecution = closeResponseAfterExecution;
            return this;
        }
        
        public Builder threadPoolSize(int threadPoolSize) {
            this.threadPoolSize = threadPoolSize;
            return this;
        }
        
        public HttpClientConfig build() {
            return new HttpClientConfig(this);
        }
    }
} 