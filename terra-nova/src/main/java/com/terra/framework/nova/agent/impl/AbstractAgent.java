package com.terra.framework.nova.agent.impl;

import com.terra.framework.nova.agent.*;
import com.terra.framework.nova.agent.memory.MemoryManager;
import com.terra.framework.nova.agent.tool.ToolRegistry;
import com.terra.framework.nova.llm.service.AIService;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Agent代理抽象基类，提供公共实现
 *
 * @author terra-nova
 */
@Slf4j
public abstract class AbstractAgent implements Agent {

    @Getter
    protected final AgentConfig config;
    protected final AIService aiService;
    protected final ToolRegistry toolRegistry;
    protected final MemoryManager memoryManager;
    protected final ScheduledExecutorService executorService;

    /**
     * 构造函数
     *
     * @param aiService AI服务
     * @param toolRegistry 工具注册表
     * @param config 代理配置
     * @param memoryManager 记忆管理器
     * @param executorService 执行器服务
     */
    protected AbstractAgent(
            AIService aiService,
            ToolRegistry toolRegistry,
            AgentConfig config,
            MemoryManager memoryManager,
            ScheduledExecutorService executorService) {
        this.aiService = aiService;
        this.toolRegistry = toolRegistry;
        this.config = config;
        this.memoryManager = memoryManager;
        this.executorService = executorService;
    }

    @Override
    public AgentResponse execute(String task, Map<String, Object> parameters) {
        try {
            AgentExecutionTrace trace = executeWithTrace(task, parameters);
            return trace.getResponse();
        } catch (Exception e) {
            log.error("Agent execution error", e);
            AgentResponse response = AgentResponse.builder()
                    .success(false)
                    .errorMessage("代理执行错误: " + e.getMessage())
                    .build();
            return response;
        }
    }

    @Override
    public void reset() {
        memoryManager.clear();
    }

    /**
     * 执行工具
     *
     * @param action 工具动作
     * @return 执行结果
     */
    protected String executeTool(AgentAction action) {
        if (action.getType() != AgentActionType.TOOL_CALL) {
            return "不是有效的工具调用";
        }
        
        String toolName = action.getTool();
        Map<String, Object> parameters = action.getParameters();
        
        if (!toolRegistry.hasTool(toolName)) {
            return "找不到工具: " + toolName;
        }
        
        try {
            Object result = toolRegistry.executeTool(toolName, parameters);
            return result != null ? result.toString() : "工具执行完成，无返回结果";
        } catch (Exception e) {
            log.error("Tool execution error", e);
            return "工具执行错误: " + e.getMessage();
        }
    }
    
    /**
     * 创建带默认参数的执行上下文
     *
     * @param task 任务
     * @param parameters 参数
     * @return 上下文Map
     */
    protected Map<String, Object> createExecutionContext(String task, Map<String, Object> parameters) {
        Map<String, Object> context = new HashMap<>(parameters != null ? parameters : new HashMap<>());
        context.put("task", task);
        context.put("agent_name", config.getName());
        context.put("max_steps", config.getMaxSteps());
        context.put("model_id", config.getModelId());
        context.put("agent_type", config.getType().name());
        
        // 添加记忆中的上下文
        if (!memoryManager.getAll().isEmpty()) {
            context.put("memory", memoryManager.getAll());
        }
        
        return context;
    }
} 