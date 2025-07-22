package com.terra.framework.autoconfigure.crust.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terra.framework.autoconfigure.bedrock.config.httpclient.HttpClientAutoConfiguration;
import com.terra.framework.autoconfigure.bedrock.config.json.JsonAutoConfiguration;
import com.terra.framework.autoconfigure.bedrock.properties.httpclient.HttpclientConnectProperties;
import com.terra.framework.autoconfigure.crust.interceptor.TraceIdRequestInterceptor;
import com.terra.framework.autoconfigure.crust.trace.TraceContextHolder;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.web.client.RestClientAutoConfiguration;
import org.springframework.boot.autoconfigure.web.reactive.function.client.WebClientAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

/**
 * {@link AutoConfiguration} for {@link RestClient} and {@link WebClient}.
 *
 * @author Terra Framework Team
 */
@AutoConfiguration(after = {HttpClientAutoConfiguration.class, TerraTraceAutoConfiguration.class, JsonAutoConfiguration.class}, before = {RestClientAutoConfiguration.class, WebClientAutoConfiguration.class})
@ConditionalOnClass({CloseableHttpClient.class, RestClient.class, WebClient.class})
public class TerraWebClientAutoConfiguration {

    @Bean
    public RestClient.Builder restClientBuilder(
        CloseableHttpClient closeableHttpClient,
        HttpclientConnectProperties httpclientConnectProperties,
        TraceIdRequestInterceptor traceIdRequestInterceptor,
        ObjectMapper objectMapper) {

        HttpComponentsClientHttpRequestFactory factory = new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
        factory.setConnectionRequestTimeout(httpclientConnectProperties.getConnectionRequestTimeout());
        factory.setConnectTimeout(httpclientConnectProperties.getConnectTimeout());

        return RestClient.builder()
            .requestFactory(factory)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .defaultStatusHandler(new DefaultResponseErrorHandler())
            .messageConverters(converters -> {
                converters.clear();
                converters.add(new StringHttpMessageConverter(StandardCharsets.UTF_8));
                converters.add(new MappingJackson2HttpMessageConverter(objectMapper));
            })
            .requestInterceptor(traceIdRequestInterceptor);
    }

    @Bean
    public WebClient.Builder webClientBuilder(HttpclientConnectProperties httpclientConnectProperties, TraceContextHolder traceContextHolder) {
        // 将 Apache HttpClient 配置适配到 Reactor Netty
        ConnectionProvider connectionProvider = ConnectionProvider.builder("terra-http-client-provider")
            .maxConnections(httpclientConnectProperties.getRequestMaxNum())
            .pendingAcquireTimeout(Duration.ofMillis(httpclientConnectProperties.getConnectTimeout()))
            .build();
        HttpClient reactorHttpClient = HttpClient.create(connectionProvider);
        ReactorClientHttpConnector connector = new ReactorClientHttpConnector(reactorHttpClient);

        // 创建 Trace ID 过滤器
        ExchangeFilterFunction traceIdFilter = (request, next) -> {
            ClientRequest.Builder builder = ClientRequest.from(request);
            String traceId = traceContextHolder.getTraceId();
            if (traceId != null) {
                builder.header("X-Trace-Id", traceId);
            }
            return next.exchange(builder.build());
        };

        return WebClient.builder()
            .clientConnector(connector)
            .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
            .filter(traceIdFilter);
    }
}
