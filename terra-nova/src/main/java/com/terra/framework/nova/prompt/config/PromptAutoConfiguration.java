package com.terra.framework.nova.prompt.config;

import com.terra.framework.nova.prompt.template.PromptTemplateLoader;
import com.terra.framework.nova.prompt.template.PromptTemplateRegistry;
import com.terra.framework.nova.prompt.template.TemplateEngine;
import com.terra.framework.nova.prompt.template.impl.FilePromptTemplateLoader;
import com.terra.framework.nova.prompt.template.impl.StringTemplateEngine;
import com.terra.framework.nova.prompt.properties.PromptProperties;
import com.terra.framework.nova.prompt.service.PromptService;
import com.terra.framework.nova.prompt.service.impl.DefaultPromptService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnResource;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.io.ResourceLoader;

/**
 * 提示词自动配置类，当存在*.prompt文件时自动装配
 *
 * @author terra-nova
 */
@ConditionalOnResource(resources = "classpath*:/**/*.prompt")
@EnableConfigurationProperties(PromptProperties.class)
public class PromptAutoConfiguration {

    /**
     * 模板引擎
     *
     * @return 模板引擎
     */
    @Bean
    public TemplateEngine templateEngine() {
        return new StringTemplateEngine();
    }

    /**
     * 模板注册表
     *
     * @param templateEngine 模板引擎
     * @param resourceLoader 资源加载器
     * @param properties     配置属性
     * @return 模板注册表
     */
    @Bean
    public PromptTemplateRegistry promptTemplateRegistry(
        TemplateEngine templateEngine,
        ResourceLoader resourceLoader,
        PromptProperties properties) {

        PromptTemplateRegistry registry = new PromptTemplateRegistry();
        PromptTemplateLoader loader = new FilePromptTemplateLoader(
            properties.getTemplatePath(),
            templateEngine,
            resourceLoader,
            properties.getTemplateExtension());
        registry.registerLoader(loader);
        return registry;
    }

    /**
     * 提示词服务
     *
     * @param registry 模板注册表
     * @return 提示词服务
     */
    @Bean
    public PromptService promptService(PromptTemplateRegistry registry) {
        return new DefaultPromptService(registry);
    }
}
