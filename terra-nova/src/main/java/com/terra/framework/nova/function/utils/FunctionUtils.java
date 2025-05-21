package com.terra.framework.nova.function.utils;

import com.terra.framework.nova.llm.model.FunctionDefinitionDetails;
import com.terra.framework.nova.llm.model.FunctionParameters;
import com.terra.framework.nova.llm.model.ToolDefinition;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 函数工具类，提供便捷创建函数定义的方法
 *
 * @author terra-nova
 */
public class FunctionUtils {

    /**
     * 创建函数定义
     *
     * @param name 函数名称
     * @param description 函数描述
     * @param parameters 参数定义
     * @return 函数定义详情
     */
    public static FunctionDefinitionDetails createFunctionDefinition(
            String name, 
            String description, 
            FunctionParameters parameters) {
        return FunctionDefinitionDetails.builder()
                .name(name)
                .description(description)
                .parameters(parameters)
                .build();
    }

    /**
     * 创建函数工具定义
     *
     * @param name 函数名称
     * @param description 函数描述
     * @param parameters 参数定义
     * @return 工具定义
     */
    public static ToolDefinition createFunctionTool(
            String name, 
            String description, 
            FunctionParameters parameters) {
        FunctionDefinitionDetails function = createFunctionDefinition(name, description, parameters);
        return ToolDefinition.ofFunction(function);
    }

    /**
     * 创建参数定义
     *
     * @param properties 属性定义
     * @param required 必需的参数名称列表
     * @return 函数参数
     */
    public static FunctionParameters createParameters(
            Map<String, Object> properties, 
            List<String> required) {
        return FunctionParameters.builder()
                .type("object")
                .properties(properties)
                .required(required)
                .build();
    }

    /**
     * 创建参数定义
     *
     * @param properties 属性定义
     * @return 函数参数（无必需参数）
     */
    public static FunctionParameters createParameters(Map<String, Object> properties) {
        return createParameters(properties, new ArrayList<>());
    }

    /**
     * 创建字符串参数定义
     *
     * @param name 参数名
     * @param description 参数描述
     * @return 参数定义Map
     */
    public static Map<String, Object> createStringParam(String name, String description) {
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "string");
        properties.put("description", description);
        param.put(name, properties);
        return param;
    }

    /**
     * 创建数字参数定义
     *
     * @param name 参数名
     * @param description 参数描述
     * @return 参数定义Map
     */
    public static Map<String, Object> createNumberParam(String name, String description) {
        Map<String, Object> param = new HashMap<>();
        Map<String, Object> properties = new HashMap<>();
        properties.put("type", "number");
        properties.put("description", description);
        param.put(name, properties);
        return param;
    }

    /**
     * 合并多个参数定义Map
     *
     * @param params 多个参数定义Map
     * @return 合并后的Map
     */
    public static Map<String, Object> mergeParams(Map<String, Object>... params) {
        Map<String, Object> result = new HashMap<>();
        for (Map<String, Object> param : params) {
            result.putAll(param);
        }
        return result;
    }
} 