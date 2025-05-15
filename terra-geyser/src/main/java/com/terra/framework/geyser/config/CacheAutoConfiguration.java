package com.terra.framework.geyser.config;

import com.terra.framework.geyser.factory.CacheFactory;
import com.terra.framework.geyser.factory.DefaultCacheFactory;
import com.terra.framework.geyser.util.GuavaCacheUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 缓存自动配置类
 *
 * @author terra
 */
public class CacheAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public CacheFactory guavaCacheFactory() {
        return new DefaultCacheFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public GuavaCacheUtils guavaCacheUtils(CacheFactory cacheFactory) {
        return new GuavaCacheUtils(cacheFactory);
    }
} 