package com.terra.framework.nova.prompt.config;

import com.terra.framework.nova.prompt.properties.CachingPromptProperties;
import com.terra.framework.nova.prompt.service.PromptService;
import com.terra.framework.nova.prompt.service.impl.CachingPromptService;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/**
 * 提示词缓存自动配置
 *
 * @author terra-nova
 */
@Configuration
@ConditionalOnBean(PromptService.class)
@AutoConfigureAfter(PromptAutoConfiguration.class)
@EnableConfigurationProperties(CachingPromptProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.prompt.cache", name = "enabled", havingValue = "true", matchIfMissing = true)
public class CachingPromptAutoConfiguration {

    @Bean
    @Primary
    public PromptService cachingPromptService(
            PromptService promptService,
            CachingPromptProperties properties) {
        return new CachingPromptService(
                promptService,
                properties.getTtlSeconds(),
                properties.getMaxSize()
        );
    }
} 