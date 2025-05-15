package com.terra.framework.strata.config.mysql;

import com.terra.framework.geyser.factory.CacheFactory;
import com.terra.framework.strata.config.mysql.adapter.CacheAdapter;
import com.terra.framework.strata.config.mysql.adapter.SqlMetricsAdapter;
import com.terra.framework.strata.config.mysql.aspect.AutoCacheAspect;
import com.terra.framework.strata.config.mysql.aspect.CacheInvalidationAspect;
import com.terra.framework.strata.config.mysql.aspect.SqlAutoCacheAspect;
import com.terra.framework.strata.config.mysql.configurer.MybatisSqlInterceptorConfigurer;
import com.terra.framework.strata.config.mysql.interceptor.SqlMonitorInterceptor;
import com.terra.framework.strata.config.mysql.manager.AutoCacheManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Bean;

/**
 * SQL自动缓存配置
 * 注册SQL监控和自动缓存相关组件
 */
@Slf4j
@AutoConfigureAfter({RedisCacheAutoConfiguration.class, TerraDruidAutoConfiguration.class})
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
    public SqlMetricsAdapter sqlMetricsCollector(AutoCacheManager autoCacheManager) {
        log.info("初始化SQL指标收集器");
        return new SqlMetricsAdapter(autoCacheManager);
    }

    /**
     * 注册缓存处理器
     */
    @Bean
    public CacheAdapter cacheAdapter(AutoCacheManager autoCacheManager, SqlMetricsAdapter sqlMetricsAdapter) {
        log.info("初始化缓存指标收集器");
        return new CacheAdapter(autoCacheManager, sqlMetricsAdapter);
    }

    /**
     * 注册SQL监控拦截器
     */
    @Bean
    @ConditionalOnBean(SqlMetricsAdapter.class)
    public SqlMonitorInterceptor sqlMonitorInterceptor(SqlMetricsAdapter metricsCollector) {
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

    @Bean
    @ConditionalOnBean({AutoCacheManager.class, CacheInvalidationAspect.class})
    public AutoCacheAspect autoCacheAspect(AutoCacheManager cacheManager, CacheInvalidationAspect invalidationAspect) {
        return new AutoCacheAspect(cacheManager, invalidationAspect);
    }

    @Bean
    @ConditionalOnBean({AutoCacheManager.class, SqlMetricsAdapter.class})
    public CacheInvalidationAspect sqlMetricsAdapter(AutoCacheManager cacheManager, SqlMetricsAdapter sqlMetricsAdapter) {
        return new CacheInvalidationAspect(sqlMetricsAdapter, cacheManager);
    }

    @Bean
    @ConditionalOnBean({AutoCacheManager.class, SqlMetricsAdapter.class})
    public SqlAutoCacheAspect sqlAutoCacheAspect(AutoCacheManager cacheManager, SqlMetricsAdapter sqlMetricsAdapter) {
        return new SqlAutoCacheAspect(sqlMetricsAdapter, cacheManager);
    }
} 