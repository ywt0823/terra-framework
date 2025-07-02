package com.terra.framework.autoconfigure.strata.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties for MySQL configuration.
 *
 * @author AI
 */
@Data
@ConfigurationProperties(prefix = "terra.mysql")
public class TerraMysqlProperties {

    /**
     * Enable mysql auto configuration.
     */
    private boolean enabled = false;

    /**
     * JDBC URL of the database.
     */
    private String url;

    /**
     * Login user of the database.
     */
    private String username;

    /**
     * Login password of the database.
     */
    private String password;

    /**
     * Fully qualified name of the JDBC driver. Auto-detected based on the URL by default.
     */
    private String driverClassName;

    /**
     * Druid specific properties.
     */
    private DruidProperties druid = new DruidProperties();

    @Data
    public static class DruidProperties {
        private int initialSize = 5;
        private int minIdle = 5;
        private int maxActive = 20;
        private long maxWait = 60000;
        private long timeBetweenEvictionRunsMillis = 60000;
        private long minEvictableIdleTimeMillis = 300000;
        private String validationQuery = "SELECT 1";
        private boolean testWhileIdle = true;
        private boolean testOnBorrow = false;
        private boolean testOnReturn = false;
        private boolean poolPreparedStatements = true;
        private int maxPoolPreparedStatementPerConnectionSize = 20;
    }
}
