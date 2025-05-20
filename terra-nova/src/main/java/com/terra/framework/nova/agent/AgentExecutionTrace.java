package com.terra.framework.nova.agent;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * Agent执行痕迹
 *
 * @author terra-nova
 */
@Data
@Builder
public class AgentExecutionTrace {

    /**
     * 最终结果
     */
    private AgentResponse response;

    /**
     * 执行步骤列表
     */
    private List<AgentStep> steps;

    /**
     * 总执行时间
     */
    private long executionTimeMs;

    /**
     * 代理内部观察
     */
    private List<String> observations;
}
