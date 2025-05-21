package com.terra.framework.nova.agent.config;

import com.terra.framework.nova.agent.AgentFactory;
import com.terra.framework.nova.agent.properties.AgentProperties;
import com.terra.framework.nova.agent.tool.ToolRegistry;
import com.terra.framework.nova.llm.config.AIServiceAutoConfiguration;
import com.terra.framework.nova.llm.service.AIService;
import com.terra.framework.nova.prompt.template.PromptTemplateRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Agent代理系统自动配置
 *
 * @author terra-nova
 */
@Slf4j
@EnableConfigurationProperties(AgentProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.agent", name = "enabled", havingValue = "true", matchIfMissing = true)
@AutoConfigureAfter(AIServiceAutoConfiguration.class)
public class AgentAutoConfiguration {

    /**
     * 创建工具注册表
     *
     * @return 工具注册表
     */
    @Bean
    @ConditionalOnMissingBean
    public ToolRegistry toolRegistry() {
        return new ToolRegistry();
    }

    /**
     * 创建代理执行器线程池
     *
     * @param properties 代理配置属性
     * @return 调度线程池
     */
    @Bean
    @ConditionalOnMissingBean(name = "agentExecutorService")
    public ScheduledExecutorService agentExecutorService(AgentProperties properties) {
        return Executors.newScheduledThreadPool(
                properties.getThreadPoolSize(),
                new CustomizableThreadFactory("agent-executor-"));
    }

    /**
     * 创建代理工厂
     *
     * @param aiService AI服务
     * @param toolRegistry 工具注册表
     * @param promptRegistry 提示词模板注册表
     * @param properties 代理配置属性
     * @param agentExecutorService 执行器服务
     * @return 代理工厂
     */
    @Bean
    @ConditionalOnMissingBean
    public AgentFactory agentFactory(
            AIService aiService,
            ToolRegistry toolRegistry,
            PromptTemplateRegistry promptRegistry,
            AgentProperties properties,
            ScheduledExecutorService agentExecutorService) {
        return new AgentFactory(aiService, toolRegistry, promptRegistry, properties, agentExecutorService);
    }
}
