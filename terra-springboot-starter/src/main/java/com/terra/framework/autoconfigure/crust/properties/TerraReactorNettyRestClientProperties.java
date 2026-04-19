package com.terra.framework.autoconfigure.crust.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.time.Duration;

/**
 * 全局 {@link org.springframework.web.client.RestClient.Builder} 的 Reactor Netty 连接池参数。
 */
@Data
@ConfigurationProperties(prefix = "terra.http.reactor-netty")
public class TerraReactorNettyRestClientProperties {

    /**
     * 是否注册 {@link org.springframework.boot.web.client.RestClientCustomizer}。
     */
    private boolean enabled = false;

    private String poolName = "terra-http";

    private int maxConnections = 50;

    private Duration maxIdleTime = Duration.ofSeconds(20);

    private Duration maxLifeTime = Duration.ofMinutes(5);

    private Duration evictInBackground = Duration.ofSeconds(30);

    private int connectTimeoutMillis = 10_000;

    private Duration responseTimeout = Duration.ofSeconds(120);
}
