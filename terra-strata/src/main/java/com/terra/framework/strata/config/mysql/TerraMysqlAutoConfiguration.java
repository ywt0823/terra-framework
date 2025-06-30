package com.terra.framework.strata.config.mysql;

import com.alibaba.druid.pool.DruidDataSource;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.OptimisticLockerInnerInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import com.google.common.collect.Lists;
import com.terra.framework.strata.exception.ValhallaDataVersionException;
import com.terra.framework.strata.properties.TerraMysqlProperties;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;

import javax.sql.DataSource;

import static com.baomidou.mybatisplus.annotation.DbType.MYSQL;

/**
 * @author ywt
 * @description MySql auto configuration
 * @date 2022年12月24日 22:05
 */
@ConditionalOnProperty(prefix = "terra.mysql", name = "enabled", havingValue = "true")
@ConditionalOnClass({DruidDataSource.class, JdbcTemplate.class})
@EnableConfigurationProperties(TerraMysqlProperties.class)
public class TerraMysqlAutoConfiguration {
    private final TerraMysqlProperties properties;

    public TerraMysqlAutoConfiguration(TerraMysqlProperties properties) {
        this.properties = properties;
    }

    @Bean("terra-datasource")
    @ConditionalOnMissingBean(DataSource.class)
    public DataSource dataSource() {
        DruidDataSource dataSource = new DruidDataSource();
        dataSource.setUrl(properties.getUrl());
        dataSource.setUsername(properties.getUsername());
        dataSource.setPassword(properties.getPassword());
        if (properties.getDriverClassName() != null) {
            dataSource.setDriverClassName(properties.getDriverClassName());
        }

        TerraMysqlProperties.DruidProperties druid = properties.getDruid();
        dataSource.setInitialSize(druid.getInitialSize());
        dataSource.setMinIdle(druid.getMinIdle());
        dataSource.setMaxActive(druid.getMaxActive());
        dataSource.setMaxWait(druid.getMaxWait());
        dataSource.setTimeBetweenEvictionRunsMillis(druid.getTimeBetweenEvictionRunsMillis());
        dataSource.setMinEvictableIdleTimeMillis(druid.getMinEvictableIdleTimeMillis());
        dataSource.setValidationQuery(druid.getValidationQuery());
        dataSource.setTestWhileIdle(druid.isTestWhileIdle());
        dataSource.setTestOnBorrow(druid.isTestOnBorrow());
        dataSource.setTestOnReturn(druid.isTestOnReturn());
        dataSource.setPoolPreparedStatements(druid.isPoolPreparedStatements());
        dataSource.setMaxPoolPreparedStatementPerConnectionSize(druid.getMaxPoolPreparedStatementPerConnectionSize());

        return dataSource;
    }

    @Bean("terra-transactionManager")
    @ConditionalOnMissingBean(DataSourceTransactionManager.class)
    public DataSourceTransactionManager transactionManager(@Qualifier("terra-datasource") DataSource datasource) {
        return new DataSourceTransactionManager(datasource);
    }

    @Bean("terra-jdbcTemplate")
    @ConditionalOnMissingBean(JdbcTemplate.class)
    public JdbcTemplate jdbcTemplate(@Qualifier("terra-datasource") DataSource datasource) {
        return new JdbcTemplate(datasource);
    }

    @Bean
    @ConditionalOnMissingBean
    public PaginationInnerInterceptor paginationInnerInterceptor() {
        PaginationInnerInterceptor paginationInnerInterceptor = new PaginationInnerInterceptor();
        paginationInnerInterceptor.setDbType(MYSQL);
        paginationInnerInterceptor.setMaxLimit(-1L);
        paginationInnerInterceptor.setOptimizeJoin(true);
        return paginationInnerInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor() {
        OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor = new OptimisticLockerInnerInterceptor();
        optimisticLockerInnerInterceptor.setException(new ValhallaDataVersionException());
        return optimisticLockerInnerInterceptor;
    }

    @Bean
    @ConditionalOnMissingBean
    public MybatisPlusInterceptor mybatisPlusInterceptor(PaginationInnerInterceptor paginationInnerInterceptor, OptimisticLockerInnerInterceptor optimisticLockerInnerInterceptor) {
        MybatisPlusInterceptor mybatisPlusInterceptor = new MybatisPlusInterceptor();
        mybatisPlusInterceptor.setInterceptors(Lists.newArrayList(paginationInnerInterceptor, optimisticLockerInnerInterceptor));
        return mybatisPlusInterceptor;
    }
} 