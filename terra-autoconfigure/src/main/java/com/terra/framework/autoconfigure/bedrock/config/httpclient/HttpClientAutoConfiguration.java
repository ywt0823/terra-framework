package com.terra.framework.autoconfigure.bedrock.config.httpclient;

import com.terra.framework.autoconfigure.bedrock.config.log.LogAutoConfiguration;
import com.terra.framework.autoconfigure.bedrock.properties.httpclient.HttpclientConnectProperties;
import com.terra.framework.common.log.LogPattern;
import com.terra.framework.common.util.httpclient.HttpClientConfig;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * HTTP客户端自动配置类
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@ConditionalOnClass(HttpClientUtils.class)
@ConditionalOnProperty(prefix = "terra.httpclient", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(HttpclientConnectProperties.class)
@AutoConfiguration(after = LogAutoConfiguration.class)
public class HttpClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public HttpClientConfig httpClientConfig(HttpclientConnectProperties properties) {
        return HttpClientConfig.builder()
                .connectTimeout(properties.getConnectTimeout())
                .readTimeout(properties.getSocketTimeout())
                .maxTotalConnections(properties.getRequestMaxNum())
                .maxConnectionsPerRoute(properties.getMaxPerRoute())
                .retryEnabled(properties.getRetryEnabled())
                .maxRetryCount(properties.getMaxRetryCount())
                .validateSSLCertificate(properties.getValidateSslCertificate())
                .closeResponseAfterExecution(true)
                .threadPoolSize(properties.getThreadPoolSize())
                .build();
    }

    @Bean
    @ConditionalOnMissingBean
    public CloseableHttpClient closeableHttpClient(HttpClientConfig httpClientConfig) {
        PoolingHttpClientConnectionManager connectionManager =
                HttpClientUtils.createConnectionManager(httpClientConfig);

        HttpClientBuilder httpClientBuilder = HttpClients.custom()
                .setConnectionManager(connectionManager)
                .setConnectionManagerShared(false)
                .setDefaultRequestConfig(HttpClientUtils.createRequestConfig(httpClientConfig));

        if (httpClientConfig.isRetryEnabled()) {
            httpClientBuilder.setRetryStrategy(
                    HttpClientUtils.createRetryStrategy(httpClientConfig.getMaxRetryCount()));
        }

        return httpClientBuilder.build();
    }

    @Bean
    @ConditionalOnMissingBean
    public HttpClientUtils httpClientUtils(CloseableHttpClient closeableHttpClient,
                                          HttpClientConfig httpClientConfig,
                                          LogPattern logPattern) {
        HttpClientUtils httpClientUtils = new HttpClientUtils(closeableHttpClient, httpClientConfig);
        logPattern.formalize("自动装配 terra-http-client 成功");
        return httpClientUtils;
    }
}
