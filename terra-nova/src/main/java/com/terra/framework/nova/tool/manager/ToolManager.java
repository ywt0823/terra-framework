package com.terra.framework.nova.tool.manager;

import com.terra.framework.nova.tool.Tool;
import com.terra.framework.nova.tool.ToolExecutionException;

import java.util.List;
import java.util.Map;

/**
 * 工具管理器接口，负责管理和执行工具
 *
 * @author terra-nova
 */
public interface ToolManager {
    
    /**
     * 注册工具
     *
     * @param tool 工具
     * @return 是否注册成功
     */
    boolean registerTool(Tool tool);
    
    /**
     * 注销工具
     *
     * @param toolName 工具名称
     * @return 是否注销成功
     */
    boolean unregisterTool(String toolName);
    
    /**
     * 获取工具
     *
     * @param toolName 工具名称
     * @return 工具
     */
    Tool getTool(String toolName);
    
    /**
     * 获取所有工具
     *
     * @return 工具列表
     */
    List<Tool> getAllTools();
    
    /**
     * 按类别获取工具
     *
     * @param category 类别
     * @return 工具列表
     */
    List<Tool> getToolsByCategory(String category);
    
    /**
     * 执行工具
     *
     * @param toolName 工具名称
     * @param parameters 参数
     * @return 执行结果
     * @throws ToolExecutionException 工具执行异常
     */
    String executeTool(String toolName, Map<String, String> parameters) throws ToolExecutionException;
    
    /**
     * 工具描述为JSON格式
     *
     * @return JSON格式的工具描述
     */
    String getToolsAsJson();
    
    /**
     * 工具描述为OpenAI格式
     *
     * @return OpenAI格式的工具描述
     */
    String getToolsAsOpenAIFormat();
} 