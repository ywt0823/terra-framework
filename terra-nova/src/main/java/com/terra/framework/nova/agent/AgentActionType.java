package com.terra.framework.nova.agent;

/**
 * 代理动作类型枚举
 *
 * @author terra-nova
 */
public enum AgentActionType {
    /**
     * 调用工具
     */
    TOOL_CALL,
    
    /**
     * 最终回答
     */
    FINAL_ANSWER,
    
    /**
     * 中间思考
     */
    INTERMEDIATE_THOUGHT,
    
    /**
     * 错误处理
     */
    ERROR_HANDLING
} 