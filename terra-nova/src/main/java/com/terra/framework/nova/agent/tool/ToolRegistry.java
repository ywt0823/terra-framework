package com.terra.framework.nova.agent.tool;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 工具注册表
 *
 * @author terra-nova
 */
public class ToolRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(ToolRegistry.class);
    
    private final Map<String, Tool> tools = new ConcurrentHashMap<>();
    
    /**
     * 注册工具
     *
     * @param tool 工具实例
     */
    public void registerTool(Tool tool) {
        tools.put(tool.getName(), tool);
        log.info("Registered tool: {}", tool.getName());
    }
    
    /**
     * 获取工具
     *
     * @param name 工具名称
     * @return 工具实例
     */
    public Tool getTool(String name) {
        return tools.get(name);
    }
    
    /**
     * 获取所有工具
     *
     * @return 所有工具
     */
    public Collection<Tool> getAllTools() {
        return tools.values();
    }
    
    /**
     * 执行工具
     *
     * @param name 工具名称
     * @param parameters 参数
     * @return 执行结果
     */
    public Object executeTool(String name, Map<String, Object> parameters) {
        Tool tool = getTool(name);
        if (tool == null) {
            throw new RuntimeException("找不到工具: " + name);
        }
        log.debug("Executing tool: {} with parameters: {}", name, parameters);
        try {
            Object result = tool.execute(parameters);
            log.debug("Tool execution completed: {}", name);
            return result;
        } catch (Exception e) {
            log.error("Error executing tool: {}", name, e);
            throw new RuntimeException("执行工具失败: " + name, e);
        }
    }
    
    /**
     * 检查工具是否存在
     *
     * @param name 工具名称
     * @return 是否存在
     */
    public boolean hasTool(String name) {
        return tools.containsKey(name);
    }
    
    /**
     * 移除工具
     *
     * @param name 工具名称
     */
    public void removeTool(String name) {
        tools.remove(name);
        log.info("Removed tool: {}", name);
    }
    
    /**
     * 清除所有工具
     */
    public void clearTools() {
        tools.clear();
        log.info("Cleared all tools");
    }
} 