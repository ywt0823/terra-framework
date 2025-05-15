package com.terra.framework.geyser.config;

import com.terra.framework.geyser.factory.CacheFactory;
import com.terra.framework.geyser.factory.DefaultCacheFactory;
import com.terra.framework.geyser.util.CacheUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

/**
 * 缓存自动配置类
 *
 * @author terra
 */
public class CacheAutoConfiguration {

    @Bean("defaultCacheFactory")
    @ConditionalOnMissingBean
    public CacheFactory guavaCacheFactory() {
        return new DefaultCacheFactory();
    }

    @Bean
    @ConditionalOnMissingBean
    public CacheUtils guavaCacheUtils(CacheFactory cacheFactory) {
        return new CacheUtils(cacheFactory);
    }
} 