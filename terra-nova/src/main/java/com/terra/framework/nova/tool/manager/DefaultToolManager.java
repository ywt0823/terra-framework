package com.terra.framework.nova.tool.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.tool.Tool;
import com.terra.framework.nova.tool.ToolExecutionException;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 默认工具管理器实现
 *
 * @author terra-nova
 */
@Slf4j
public class DefaultToolManager implements ToolManager {
    
    /**
     * 工具映射
     */
    private final Map<String, Tool> toolMap;
    
    /**
     * 工具类别映射
     */
    private final Map<String, List<String>> categoryMap;
    
    /**
     * 构造函数
     */
    public DefaultToolManager() {
        this.toolMap = new ConcurrentHashMap<>();
        this.categoryMap = new ConcurrentHashMap<>();
    }
    
    @Override
    public boolean registerTool(Tool tool) {
        if (tool == null || tool.getName() == null || tool.getName().trim().isEmpty()) {
            log.warn("工具为空或工具名称为空，无法注册");
            return false;
        }
        
        String toolName = tool.getName();
        
        // 如果已存在同名工具，记录日志并更新
        if (toolMap.containsKey(toolName)) {
            log.info("工具 '{}' 已存在，将被更新", toolName);
            
            // 从旧的类别中移除
            String oldCategory = toolMap.get(toolName).getCategory();
            if (categoryMap.containsKey(oldCategory)) {
                categoryMap.get(oldCategory).remove(toolName);
            }
        }
        
        // 注册工具
        toolMap.put(toolName, tool);
        
        // 更新类别映射
        String category = tool.getCategory();
        categoryMap.computeIfAbsent(category, k -> new ArrayList<>()).add(toolName);
        
        log.info("工具 '{}' 已注册，类别: {}", toolName, category);
        return true;
    }
    
    @Override
    public boolean unregisterTool(String toolName) {
        if (toolName == null || !toolMap.containsKey(toolName)) {
            return false;
        }
        
        Tool tool = toolMap.remove(toolName);
        
        // 从类别映射中移除
        String category = tool.getCategory();
        if (categoryMap.containsKey(category)) {
            categoryMap.get(category).remove(toolName);
            
            // 如果类别下没有工具了，删除这个类别
            if (categoryMap.get(category).isEmpty()) {
                categoryMap.remove(category);
            }
        }
        
        log.info("工具 '{}' 已注销", toolName);
        return true;
    }
    
    @Override
    public Tool getTool(String toolName) {
        return toolMap.get(toolName);
    }
    
    @Override
    public List<Tool> getAllTools() {
        return new ArrayList<>(toolMap.values());
    }
    
    @Override
    public List<Tool> getToolsByCategory(String category) {
        if (category == null || !categoryMap.containsKey(category)) {
            return Collections.emptyList();
        }
        
        return categoryMap.get(category).stream()
                .map(toolMap::get)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }
    
    @Override
    public String executeTool(String toolName, Map<String, String> parameters) throws ToolExecutionException {
        Tool tool = getTool(toolName);
        if (tool == null) {
            throw new ToolExecutionException("unknown", "TOOL_NOT_FOUND", "工具 '" + toolName + "' 不存在");
        }
        
        return tool.execute(parameters);
    }
    
    @Override
    public String getToolsAsJson() {
        JSONArray toolsArray = new JSONArray();
        
        for (Tool tool : getAllTools()) {
            JSONObject toolObj = new JSONObject();
            toolObj.put("name", tool.getName());
            toolObj.put("description", tool.getDescription());
            toolObj.put("category", tool.getCategory());
            toolObj.put("requiresAuthentication", tool.requiresAuthentication());
            toolObj.put("isAsync", tool.isAsync());
            
            // 参数描述
            JSONObject parameters = new JSONObject();
            for (Map.Entry<String, Tool.ParameterDescription> entry : tool.getParameterDescriptions().entrySet()) {
                JSONObject paramObj = new JSONObject();
                Tool.ParameterDescription paramDesc = entry.getValue();
                
                paramObj.put("name", paramDesc.getName());
                paramObj.put("description", paramDesc.getDescription());
                paramObj.put("type", paramDesc.getType());
                paramObj.put("required", paramDesc.isRequired());
                
                if (paramDesc.getDefaultValue() != null) {
                    paramObj.put("defaultValue", paramDesc.getDefaultValue());
                }
                
                parameters.put(entry.getKey(), paramObj);
            }
            
            toolObj.put("parameters", parameters);
            toolsArray.add(toolObj);
        }
        
        return toolsArray.toJSONString();
    }
    
    @Override
    public String getToolsAsOpenAIFormat() {
        JSONArray toolsArray = new JSONArray();
        
        for (Tool tool : getAllTools()) {
            JSONObject toolObj = new JSONObject();
            toolObj.put("type", "function");
            
            JSONObject function = new JSONObject();
            function.put("name", tool.getName());
            function.put("description", tool.getDescription());
            
            // 参数描述
            JSONObject parameters = new JSONObject();
            parameters.put("type", "object");
            
            JSONObject properties = new JSONObject();
            JSONArray required = new JSONArray();
            
            for (Map.Entry<String, Tool.ParameterDescription> entry : tool.getParameterDescriptions().entrySet()) {
                String paramName = entry.getKey();
                Tool.ParameterDescription paramDesc = entry.getValue();
                
                JSONObject paramObj = new JSONObject();
                paramObj.put("type", convertParamType(paramDesc.getType()));
                paramObj.put("description", paramDesc.getDescription());
                
                properties.put(paramName, paramObj);
                
                if (paramDesc.isRequired()) {
                    required.add(paramName);
                }
            }
            
            parameters.put("properties", properties);
            
            if (!required.isEmpty()) {
                parameters.put("required", required);
            }
            
            function.put("parameters", parameters);
            toolObj.put("function", function);
            
            toolsArray.add(toolObj);
        }
        
        return toolsArray.toJSONString();
    }
    
    /**
     * 转换参数类型为OpenAI格式
     *
     * @param type 参数类型
     * @return OpenAI格式参数类型
     */
    private String convertParamType(String type) {
        if (type == null) {
            return "string";
        }
        
        switch (type.toLowerCase()) {
            case "integer":
            case "int":
            case "long":
                return "integer";
            case "float":
            case "double":
            case "decimal":
                return "number";
            case "boolean":
            case "bool":
                return "boolean";
            case "array":
            case "list":
                return "array";
            case "object":
            case "map":
                return "object";
            default:
                return "string";
        }
    }
} 