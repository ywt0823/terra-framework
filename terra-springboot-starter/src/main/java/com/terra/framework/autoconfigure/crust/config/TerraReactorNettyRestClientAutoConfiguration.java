package com.terra.framework.autoconfigure.crust.config;

import com.terra.framework.autoconfigure.crust.properties.TerraReactorNettyRestClientProperties;
import io.netty.channel.ChannelOption;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.client.RestClientCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.http.client.ReactorClientHttpRequestFactory;
import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

/**
 * 可选的全局 Reactor Netty 连接池，降低远端关闭空闲连接后的复用问题（如 PrematureCloseException）。
 */
@AutoConfiguration
@EnableConfigurationProperties(TerraReactorNettyRestClientProperties.class)
@ConditionalOnClass({HttpClient.class, ReactorClientHttpRequestFactory.class})
@ConditionalOnProperty(prefix = "terra.http.reactor-netty", name = "enabled", havingValue = "true")
public class TerraReactorNettyRestClientAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(name = "terraReactorNettyRestClientCustomizer")
    public RestClientCustomizer terraReactorNettyRestClientCustomizer(TerraReactorNettyRestClientProperties props) {
        ConnectionProvider provider = ConnectionProvider.builder(props.getPoolName())
                .maxConnections(props.getMaxConnections())
                .maxIdleTime(props.getMaxIdleTime())
                .maxLifeTime(props.getMaxLifeTime())
                .evictInBackground(props.getEvictInBackground())
                .build();

        HttpClient httpClient = HttpClient.create(provider)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.getConnectTimeoutMillis())
                .responseTimeout(props.getResponseTimeout());

        ReactorClientHttpRequestFactory factory = new ReactorClientHttpRequestFactory(httpClient);

        return builder -> builder.requestFactory(factory);
    }
}
