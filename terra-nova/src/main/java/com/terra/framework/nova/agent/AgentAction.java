package com.terra.framework.nova.agent;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * 代理动作
 *
 * @author terra-nova
 */
@Data
@Builder
public class AgentAction {
    
    /**
     * 动作类型
     */
    private AgentActionType type;
    
    /**
     * 工具名称
     */
    private String tool;
    
    /**
     * 工具参数
     */
    private Map<String, Object> parameters;
} 