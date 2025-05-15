package com.terra.framework.strata.config.mysql;

import com.terra.framework.geyser.factory.CacheFactory;
import com.terra.framework.strata.helper.AutoCacheManager;
import com.terra.framework.strata.helper.SqlMetricsCollector;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * SQL自动缓存配置
 * 注册SQL监控和自动缓存相关组件
 */
@Slf4j
@Configuration
@ConditionalOnProperty(prefix = "terra.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class SqlAutoCacheAutoConfiguration {

    /**
     * 注册自动缓存管理器
     */
    @Bean
    @ConditionalOnBean({CacheFactory.class})
    public AutoCacheManager autoCacheManager(
            @Qualifier("defaultCacheFactory") CacheFactory localCacheFactory,
            @Qualifier("redisCacheFactory") CacheFactory redisCacheFactory) {
        log.info("初始化自动缓存管理器");
        return new AutoCacheManager(localCacheFactory, redisCacheFactory);
    }

    /**
     * 注册SQL指标收集器
     */
    @Bean
    @ConditionalOnBean(AutoCacheManager.class)
    public SqlMetricsCollector sqlMetricsCollector(AutoCacheManager autoCacheManager) {
        log.info("初始化SQL指标收集器");
        return new SqlMetricsCollector(autoCacheManager);
    }

    /**
     * 注册SQL监控拦截器
     */
    @Bean
    @ConditionalOnBean(SqlMetricsCollector.class)
    public SqlMonitorInterceptor sqlMonitorInterceptor(SqlMetricsCollector metricsCollector) {
        log.info("初始化SQL监控拦截器");
        return new SqlMonitorInterceptor(metricsCollector);
    }
    
    /**
     * 将SQL监控拦截器加入MybatisPlus拦截器链
     */
    @Bean
    @ConditionalOnBean(SqlMonitorInterceptor.class)
    public MybatisSqlInterceptorConfigurer mybatisSqlInterceptorConfigurer(SqlMonitorInterceptor sqlMonitorInterceptor) {
        return new MybatisSqlInterceptorConfigurer(sqlMonitorInterceptor);
    }
} 