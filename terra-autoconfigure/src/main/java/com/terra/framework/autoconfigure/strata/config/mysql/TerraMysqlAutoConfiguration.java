package com.terra.framework.autoconfigure.strata.config.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import org.springframework.boot.autoconfigure.AutoConfigureOrder;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

/**
 * @author ywt
 * @description MySql auto configuration
 * @date 2022年12月24日 22:05
 */
@ConditionalOnClass(DataSourceProperties.class)
@ConditionalOnProperty(prefix = "spring.datasource.druid", name = "url")
@AutoConfigureOrder(Ordered.HIGHEST_PRECEDENCE)
public class TerraMysqlAutoConfiguration {


    @Bean
    @ConfigurationProperties(prefix = "spring.datasource.druid")
    @Primary
    public DataSource dataSource() {
        return new DruidDataSource();
    }

    @Bean
    @Primary
    public DataSourceTransactionManager transactionManager(DataSource datasource) {
        return new DataSourceTransactionManager(datasource);
    }

    @Bean
    @Primary
    public JdbcTemplate jdbcTemplate(DataSource datasource) {
        return new JdbcTemplate(datasource);
    }
}
