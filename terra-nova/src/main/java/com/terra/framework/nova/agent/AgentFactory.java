package com.terra.framework.nova.agent;

import com.terra.framework.nova.agent.impl.PlanAndExecuteAgent;
import com.terra.framework.nova.agent.impl.ReActAgent;
import com.terra.framework.nova.agent.memory.DefaultMemoryManager;
import com.terra.framework.nova.agent.memory.MemoryManager;
import com.terra.framework.nova.agent.properties.AgentProperties;
import com.terra.framework.nova.agent.tool.ToolRegistry;
import com.terra.framework.nova.llm.service.AIService;
import com.terra.framework.nova.prompt.template.PromptTemplate;
import com.terra.framework.nova.prompt.template.PromptTemplateRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.ScheduledExecutorService;

/**
 * Agent工厂
 *
 * @author terra-nova
 */
@Slf4j
public class AgentFactory {

    private final AIService aiService;
    private final ToolRegistry toolRegistry;
    private final PromptTemplateRegistry promptRegistry;
    private final AgentProperties properties;
    private final ScheduledExecutorService executorService;

    /**
     * 构造函数
     *
     * @param aiService       AI服务
     * @param toolRegistry    工具注册表
     * @param promptRegistry  提示词模板注册表
     * @param properties      代理配置属性
     * @param executorService 执行器服务
     */
    public AgentFactory(
        AIService aiService,
        ToolRegistry toolRegistry,
        PromptTemplateRegistry promptRegistry,
        AgentProperties properties,
        ScheduledExecutorService executorService) {
        this.aiService = aiService;
        this.toolRegistry = toolRegistry;
        this.promptRegistry = promptRegistry;
        this.properties = properties;
        this.executorService = executorService;

        log.info("AgentFactory initialized with default model: {}", properties.getDefaultModelId());
    }

    /**
     * 创建ReAct代理
     *
     * @param config 代理配置
     * @return ReAct代理实例
     */
    public Agent createReActAgent(AgentConfig config) {
        PromptTemplate promptTemplate = promptRegistry.getTemplate(properties.getReact().getPromptTemplateId());
        MemoryManager memoryManager = new DefaultMemoryManager();

        log.info("Creating ReAct agent with model: {}", config.getModelId());
        return new ReActAgent(
            aiService,
            toolRegistry,
            config,
            promptTemplate,
            memoryManager,
            executorService);
    }

    /**
     * 创建规划-执行代理
     *
     * @param config 代理配置
     * @return 规划-执行代理实例
     */
    public Agent createPlanAndExecuteAgent(AgentConfig config) {
        PromptTemplate plannerTemplate = promptRegistry.getTemplate(
            properties.getPlanAndExecute().getPlannerPromptTemplateId());
        PromptTemplate executorTemplate = promptRegistry.getTemplate(
            properties.getPlanAndExecute().getExecutorPromptTemplateId());
        MemoryManager memoryManager = new DefaultMemoryManager();

        log.info("Creating Plan-and-Execute agent with model: {}", config.getModelId());
        return new PlanAndExecuteAgent(
            aiService,
            toolRegistry,
            config,
            plannerTemplate,
            executorTemplate,
            memoryManager,
            executorService);
    }

    /**
     * 根据类型创建代理
     *
     * @param type   代理类型
     * @param config 代理配置
     * @return 代理实例
     */
    public Agent createAgent(AgentType type, AgentConfig config) {
        return switch (type) {
            case REACT -> createReActAgent(config);
            case PLAN_AND_EXECUTE -> createPlanAndExecuteAgent(config);
            case CONVERSATIONAL -> throw new UnsupportedOperationException("对话式代理尚未实现");
        };
    }

    /**
     * 使用默认配置创建代理
     *
     * @param type 代理类型
     * @return 代理实例
     */
    public Agent createAgent(AgentType type) {
        DefaultAgentConfig config = new DefaultAgentConfig();
        config.setModelId(properties.getDefaultModelId());
        config.setType(type);
        config.setMaxSteps(properties.getMaxSteps());
        config.setTimeoutMs(properties.getTimeoutMs());
        config.setLlmParameters(properties.getDefaultLlmParameters());

        return createAgent(type, config);
    }

    /**
     * 使用默认类型和配置创建代理
     *
     * @return 代理实例
     */
    public Agent createDefaultAgent() {
        return createAgent(properties.getDefaultType());
    }
}
