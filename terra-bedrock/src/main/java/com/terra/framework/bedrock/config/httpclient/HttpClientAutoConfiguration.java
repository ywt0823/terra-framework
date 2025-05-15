package com.terra.framework.bedrock.config.httpclient;

import com.terra.framework.bedrock.config.log.LogAutoConfiguration;
import com.terra.framework.bedrock.properties.httpclient.HttpclientConnectProperties;
import com.terra.framework.common.log.LogPattern;
import com.terra.framework.common.util.httpclient.HttpClientUtils;
import org.apache.hc.client5.http.config.ConnectionConfig;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.DefaultHttpRequestRetryStrategy;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClientBuilder;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author ywt
 * @description
 * @date 2022年12月24日 23:37
 */
@ConditionalOnClass(HttpclientConnectProperties.class)
@ConditionalOnProperty(prefix = "terra.httpclient", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(HttpclientConnectProperties.class)
@AutoConfiguration(after = LogAutoConfiguration.class)
public class HttpClientAutoConfiguration {


    @Bean
    public HttpClientUtils httpClientUtils(CloseableHttpClient closeableHttpClient,
                                           HttpclientConnectProperties httpclientConnectProperties,
                                           LogPattern logPattern) {
        HttpClientUtils httpClientUtils = new HttpClientUtils(closeableHttpClient, getRequestConfig(httpclientConnectProperties));
        logPattern.formalize("自动装配 terra-clients 成功");
        return httpClientUtils;
    }

    @Bean
    public CloseableHttpClient closeableHttpClient(HttpclientConnectProperties httpclientConnectProperties) {
        RequestConfig requestConfig = getRequestConfig(httpclientConnectProperties);
        //连接池管理器
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        ConnectionConfig connConfig = ConnectionConfig.custom().build();
        SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(Timeout.ofMinutes(httpclientConnectProperties.getConnectTimeout())).build();
        poolingHttpClientConnectionManager.setDefaultConnectionConfig(connConfig);
        poolingHttpClientConnectionManager.setDefaultSocketConfig(socketConfig);
        // 连接池最大生成连接数
        poolingHttpClientConnectionManager.setMaxTotal(httpclientConnectProperties.getRequestMaxNum());
        // 默认设置route最大连接数
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(httpclientConnectProperties.getMaxPerRoute());
        //创建builder
        HttpClientBuilder httpClientBuilder = HttpClients.custom();
        //设置管理器
        httpClientBuilder.setConnectionManager(poolingHttpClientConnectionManager).setConnectionManagerShared(true);
        // 长连接策略
        httpClientBuilder.setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        return httpClientBuilder.setDefaultRequestConfig(requestConfig).setRetryStrategy(new DefaultHttpRequestRetryStrategy()).build();
    }

    private static RequestConfig getRequestConfig(HttpclientConnectProperties httpclientConnectProperties) {
        return RequestConfig.custom()
                //请求超时时间
                .setConnectionRequestTimeout(Timeout.ofMinutes(httpclientConnectProperties.getConnectionRequestTimeout()))
                //连接超时时间
                .setConnectTimeout(Timeout.ofMinutes(httpclientConnectProperties.getConnectTimeout())).build();
    }

}
