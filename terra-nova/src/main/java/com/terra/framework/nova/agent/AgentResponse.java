package com.terra.framework.nova.agent;

import lombok.Builder;
import lombok.Data;

import java.util.Map;

/**
 * Agent执行结果
 *
 * @author terra-nova
 */
@Data
@Builder
public class AgentResponse {
    
    /**
     * 最终输出内容
     */
    private String output;
    
    /**
     * 执行是否成功
     */
    private boolean success;
    
    /**
     * 错误信息
     */
    private String errorMessage;
    
    /**
     * 执行元数据
     */
    private Map<String, Object> metadata;
    
    /**
     * 使用的工具及次数
     */
    private Map<String, Integer> toolUsage;
} 