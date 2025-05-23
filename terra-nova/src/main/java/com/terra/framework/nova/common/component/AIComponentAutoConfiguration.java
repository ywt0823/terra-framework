package com.terra.framework.nova.common.component;

import com.terra.framework.nova.agent.config.AgentAutoConfiguration;
import com.terra.framework.nova.agent.tool.ToolRegistry;
import com.terra.framework.nova.common.annotation.AIComponent;
import com.terra.framework.nova.common.annotation.AIParameter;
import com.terra.framework.nova.common.annotation.ComponentType;
import com.terra.framework.nova.function.FunctionRegistry;
import com.terra.framework.nova.function.config.AIFunctionAutoConfiguration;
import com.terra.framework.nova.llm.config.AIServiceAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * AI组件自动配置类
 *
 * @author terra-nova
 */
@Slf4j
@EnableConfigurationProperties(AIComponentProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.component", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter({AIServiceAutoConfiguration.class, AgentAutoConfiguration.class, AIFunctionAutoConfiguration.class})
public class AIComponentAutoConfiguration {

    /**
     * 创建组件管理器
     *
     * @param toolRegistry     工具注册表
     * @param functionRegistry 函数注册表
     * @return 组件管理器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnBean({ToolRegistry.class, FunctionRegistry.class})
    public AIComponentManager aiComponentManager(
            ToolRegistry toolRegistry,
            FunctionRegistry functionRegistry) {
        log.info("Creating AIComponentManager");
        return new AIComponentManager(toolRegistry, functionRegistry);
    }

    /**
     * 创建组件适配器
     *
     * @return 组件适配器
     */
    @Bean
    @ConditionalOnMissingBean
    public AIComponentAdapter aiComponentAdapter() {
        log.info("Creating AIComponentAdapter");
        return new AIComponentAdapter();
    }

    /**
     * 创建组件扫描器
     *
     * @param componentManager 组件管理器
     * @param componentAdapter 组件适配器
     * @param properties       配置属性
     * @return 组件扫描器
     */
    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "terra.nova.component", name = "auto-register", havingValue = "true", matchIfMissing = true)
    public AIComponentScanner aiComponentScanner(
            AIComponentManager componentManager,
            AIComponentAdapter componentAdapter,
            AIComponentProperties properties) {
        log.info("Creating AIComponentScanner");
        return new AIComponentScanner(
                componentManager,
                componentAdapter,
                properties.getBasePackages()
        );
    }


}
