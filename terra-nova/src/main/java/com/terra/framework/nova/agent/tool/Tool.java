package com.terra.framework.nova.agent.tool;

import java.util.List;
import java.util.Map;

/**
 * 工具接口
 *
 * @author terra-nova
 */
public interface Tool {
    
    /**
     * 获取工具名称
     *
     * @return 工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     *
     * @return 工具描述
     */
    String getDescription();
    
    /**
     * 获取工具参数描述
     *
     * @return 参数描述列表
     */
    List<ToolParameter> getParameters();
    
    /**
     * 执行工具
     *
     * @param parameters 参数
     * @return 执行结果
     */
    Object execute(Map<String, Object> parameters);
} 