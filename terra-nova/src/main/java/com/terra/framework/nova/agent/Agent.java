package com.terra.framework.nova.agent;

import java.util.Map;

/**
 * Agent代理接口
 *
 * @author terra-nova
 */
public interface Agent {
    
    /**
     * 执行指定任务
     *
     * @param task 任务描述
     * @param parameters 执行参数
     * @return 代理执行结果
     */
    AgentResponse execute(String task, Map<String, Object> parameters);
    
    /**
     * 执行并返回详细执行痕迹
     *
     * @param task 任务描述
     * @param parameters 执行参数
     * @return 含执行痕迹的结果
     */
    AgentExecutionTrace executeWithTrace(String task, Map<String, Object> parameters);
    
    /**
     * 使用对话ID执行任务
     * 
     * @param task 任务描述
     * @param parameters 执行参数
     * @param conversationId 会话ID
     * @return 代理执行结果
     */
    default AgentResponse executeWithConversation(String task, Map<String, Object> parameters, String conversationId) {
        if (parameters == null) {
            parameters = Map.of("conversationId", conversationId);
        } else if (!parameters.containsKey("conversationId")) {
            parameters.put("conversationId", conversationId);
        }
        return execute(task, parameters);
    }
    
    /**
     * 获取代理配置
     *
     * @return 代理配置
     */
    AgentConfig getConfig();
    
    /**
     * 重置代理状态
     */
    void reset();
} 