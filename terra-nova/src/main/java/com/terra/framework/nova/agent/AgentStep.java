package com.terra.framework.nova.agent;

import lombok.Builder;
import lombok.Data;

/**
 * 代理执行步骤
 *
 * @author terra-nova
 */
@Data
@Builder
public class AgentStep {
    
    /**
     * 步骤ID
     */
    private String id;
    
    /**
     * 当前思考
     */
    private String thought;
    
    /**
     * 选择的动作
     */
    private AgentAction action;
    
    /**
     * 动作结果
     */
    private String actionResult;
    
    /**
     * 步骤执行时间
     */
    private long stepTimeMs;
} 