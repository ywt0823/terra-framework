package com.terra.framework.nova.agent;

import java.util.List;
import java.util.Map;

/**
 * Agent配置接口
 *
 * @author terra-nova
 */
public interface AgentConfig {
    
    /**
     * 获取最大执行步骤数
     *
     * @return 最大步骤数
     */
    int getMaxSteps();
    
    /**
     * 获取超时时间(毫秒)
     *
     * @return 超时时间
     */
    long getTimeoutMs();
    
    /**
     * 获取LLM模型ID
     *
     * @return 模型ID
     */
    String getModelId();
    
    /**
     * 获取代理类型
     *
     * @return 代理类型
     */
    AgentType getType();
    
    /**
     * 获取可用工具列表
     *
     * @return 工具ID列表
     */
    List<String> getTools();
    
    /**
     * 获取代理名称
     *
     * @return 代理名称
     */
    String getName();
    
    /**
     * 获取LLM参数
     *
     * @return LLM参数
     */
    Map<String, Object> getLlmParameters();
} 