package com.terra.framework.nova.prompt.config;

import com.terra.framework.nova.prompt.engine.DefaultPromptEngineService;
import com.terra.framework.nova.prompt.engine.PromptEngineService;
import com.terra.framework.nova.prompt.properties.PromptProperties;
import com.terra.framework.nova.prompt.strategy.PromptStrategy;
import com.terra.framework.nova.prompt.strategy.SimplePromptStrategy;
import com.terra.framework.nova.prompt.template.PromptTemplate;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示引擎自动配置类
 *
 * @author terra-nova
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(PromptProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.prompt", name = "enabled", havingValue = "true", matchIfMissing = true)
public class PromptAutoConfiguration {

    /**
     * 配置简单提示策略
     */
    @Bean
    @ConditionalOnMissingBean(name = "simplePromptStrategy")
    public PromptStrategy simplePromptStrategy(PromptProperties properties) {
        PromptProperties.StrategyProperties strategyProps = properties.getStrategies().get("simple");

        if (strategyProps != null) {
            log.info("使用配置的简单提示策略");
            return new SimplePromptStrategy(
                    strategyProps.getName(),
                    strategyProps.getPromptTemplate(),
                    strategyProps.getSystemPrompt(),
                    strategyProps.getMaxHistoryMessages()
            );
        } else {
            log.info("使用默认的简单提示策略");
            return new SimplePromptStrategy();
        }
    }

    /**
     * 配置详细提示策略
     */
    @Bean
    @ConditionalOnMissingBean(name = "detailedPromptStrategy")
    public PromptStrategy detailedPromptStrategy(PromptProperties properties) {
        PromptProperties.StrategyProperties strategyProps = properties.getStrategies().get("detailed");

        if (strategyProps != null) {
            log.info("使用配置的详细提示策略");
            return new SimplePromptStrategy(
                    strategyProps.getName(),
                    strategyProps.getPromptTemplate(),
                    strategyProps.getSystemPrompt(),
                    strategyProps.getMaxHistoryMessages()
            );
        } else {
            // 使用默认配置
            Map<String, PromptProperties.StrategyProperties> defaultStrategies = properties.getDefaultStrategies();
            PromptProperties.StrategyProperties defaultProps = defaultStrategies.get("detailed");

            log.info("使用默认的详细提示策略");
            return new SimplePromptStrategy(
                    "detailed",
                    defaultProps.getPromptTemplate(),
                    defaultProps.getSystemPrompt(),
                    defaultProps.getMaxHistoryMessages()
            );
        }
    }

    /**
     * 配置所有提示策略
     */
    @Bean
    @ConditionalOnMissingBean(name = "promptStrategies")
    public Map<String, PromptStrategy> promptStrategies(
            PromptStrategy simplePromptStrategy,
            PromptStrategy detailedPromptStrategy) {
        Map<String, PromptStrategy> strategies = new HashMap<>();
        strategies.put(simplePromptStrategy.getName(), simplePromptStrategy);
        strategies.put(detailedPromptStrategy.getName(), detailedPromptStrategy);
        log.info("已配置提示策略: {}", strategies.keySet());
        return strategies;
    }

    /**
     * 配置默认的提示模板
     */
    @Bean
    @ConditionalOnMissingBean(name = "defaultPromptTemplates")
    public List<PromptTemplate> defaultPromptTemplates(PromptProperties properties) {
        List<PromptTemplate> templates = new ArrayList<>();

        // 如果配置了模板，使用配置的模板
        if (!properties.getTemplates().isEmpty()) {
            log.info("使用配置的提示模板");
            for (PromptProperties.TemplateProperties templateProps : properties.getTemplates()) {
                PromptTemplate template = PromptTemplate.builder()
                        .name(templateProps.getName())
                        .description(templateProps.getDescription())
                        .template(templateProps.getTemplate())
                        .type(templateProps.getType())
                        .build();
                templates.add(template);
            }
        } else {
            // 使用默认模板
            log.info("使用默认的提示模板");
            List<PromptProperties.TemplateProperties> defaultTemplates = properties.getDefaultTemplates();
            for (PromptProperties.TemplateProperties templateProps : defaultTemplates) {
                PromptTemplate template = PromptTemplate.builder()
                        .name(templateProps.getName())
                        .description(templateProps.getDescription())
                        .template(templateProps.getTemplate())
                        .type(templateProps.getType())
                        .build();
                templates.add(template);
            }
        }

        log.info("已配置提示模板: {}", templates.size());
        return templates;
    }

    /**
     * 配置提示引擎服务
     */
    @Bean
    @ConditionalOnMissingBean(PromptEngineService.class)
    public PromptEngineService promptEngineService(List<PromptTemplate> defaultPromptTemplates) {
        DefaultPromptEngineService engineService = new DefaultPromptEngineService();

        // 注册默认模板
        engineService.registerTemplates(defaultPromptTemplates);

        log.info("初始化提示引擎服务");
        return engineService;
    }
}
