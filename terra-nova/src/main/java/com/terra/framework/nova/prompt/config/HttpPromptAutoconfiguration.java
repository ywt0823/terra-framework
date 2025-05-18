package com.terra.framework.nova.prompt.config;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.prompt.properties.HttpPromptProperties;
import com.terra.framework.nova.prompt.template.PromptTemplateLoader;
import com.terra.framework.nova.prompt.template.TemplateEngine;
import com.terra.framework.nova.prompt.template.impl.HttpPromptTemplateLoader;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * HTTP提示词模板加载器配置
 *
 * @author terra-nova
 */
@ConditionalOnBean({HttpClientUtils.class, TemplateEngine.class})
@AutoConfigureAfter(PromptAutoConfiguration.class)
@EnableConfigurationProperties(HttpPromptProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.prompt.http", name = "enabled", havingValue = "true")
public class HttpPromptAutoconfiguration {

    /**
     * 配置HTTP提示词模板加载器
     *
     * @param engine          模板引擎
     * @param httpClientUtils HTTP客户端工具
     * @param properties      HTTP提示词属性配置
     * @return HTTP提示词模板加载器
     */
    @Bean("httpPromptTemplateLoader")
    public PromptTemplateLoader httpPromptTemplateLoader(
        TemplateEngine engine,
        HttpClientUtils httpClientUtils,
        HttpPromptProperties properties) {

        Map<String, String> authHeaders = null;

        // 如果设置了认证令牌，则添加认证头
        String authToken = properties.getAuthToken();
        if (authToken != null && !authToken.isEmpty()) {
            authHeaders = new HashMap<>();
            authHeaders.put(properties.getAuthHeaderName(), "Bearer " + authToken);
        }

        return new HttpPromptTemplateLoader(
            httpClientUtils,
            engine,
            properties.getBaseUrl(),
            properties.getTemplatePath(),
            authHeaders,
            TimeUnit.MINUTES.toMillis(properties.getCacheTtlMinutes())
        );
    }
}
