package com.terra.framework.strata.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for Redis configuration.
 *
 * @author AI
 */
@Data
@ConfigurationProperties(prefix = "terra.redis")
public class TerraRedisProperties {

    /**
     * Enable redis auto configuration.
     */
    private boolean enabled = false;

    /**
     * Database index used by the connection factory.
     */
    private int database = 0;

    /**
     * Redis server host.
     */
    private String host = "localhost";

    /**
     * Redis server port.
     */
    private int port = 6379;

    /**
     * Login password of the redis server.
     */
    private String password;

    /**
     * Login user of the redis server.
     */
    private String username;

    /**
     * Redisson specific properties
     */
    private RedissonProperties redisson = new RedissonProperties();

    @Data
    public static class RedissonProperties {
        private int timeout = 3000;
        private int connectionPoolSize = 64;
        private int connectionMinimumIdleSize = 10;
    }
} 