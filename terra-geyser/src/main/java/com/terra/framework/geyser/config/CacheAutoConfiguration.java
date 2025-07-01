package com.terra.framework.geyser.config;

import com.terra.framework.geyser.factory.CacheFactory;
import com.terra.framework.geyser.factory.DefaultCacheFactory;
import com.terra.framework.geyser.factory.RedissonCacheFactory;
import com.terra.framework.geyser.monitor.CacheMonitorService;
import com.terra.framework.geyser.util.CacheUtils;
import org.redisson.api.RedissonClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * 缓存自动配置类
 *
 * @author terra
 */
@EnableConfigurationProperties(CacheProperties.class)
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnClass(RedissonClient.class)
    public CacheFactory RedissonCacheFactory(RedissonClient redissonClient, CacheProperties cacheProperties) {
        return new RedissonCacheFactory(redissonClient, cacheProperties);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheFactory guavaCacheFactory() {
        return new DefaultCacheFactory();
    }


    @Bean
    @ConditionalOnMissingBean
    public CacheUtils cacheUtils(CacheFactory cacheFactory) {
        return new CacheUtils(cacheFactory);
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheMonitorService cacheMonitorService(CacheUtils cacheUtils, CacheProperties properties) {
        return new CacheMonitorService(cacheUtils, properties);
    }


}
