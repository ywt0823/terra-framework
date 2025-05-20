package com.terra.framework.nova.agent.tool.converter;

import com.terra.framework.nova.agent.tool.Tool;
import com.terra.framework.nova.agent.tool.ToolParameter;
import com.terra.framework.nova.agent.tool.annotation.AITool;
import com.terra.framework.nova.agent.tool.annotation.AIToolParameter;
import com.terra.framework.nova.agent.tool.handler.MethodToolHandler;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.List;

/**
 * 方法到工具转换器
 * 将带有@AITool注解的方法转换为Tool实现
 *
 * @author terra-nova
 */
@Slf4j
public class MethodToToolConverter {

    /**
     * 转换方法为工具
     *
     * @param method 带有@AITool注解的方法
     * @param target 方法所属对象
     * @return Tool实现
     */
    public Tool convert(Method method, Object target) {
        AITool annotation = method.getAnnotation(AITool.class);
        if (annotation == null) {
            throw new IllegalArgumentException("方法没有@AITool注解: " + method.getName());
        }

        String name = getToolName(method, annotation);
        String description = annotation.description();
        List<ToolParameter> parameters = extractParameters(method);

        log.info("转换方法为工具: {} -> {}", method.getName(), name);
        return new MethodToolHandler(name, description, parameters, target, method);
    }

    /**
     * 获取工具名称
     *
     * @param method 方法
     * @param annotation 注解
     * @return 工具名称
     */
    private String getToolName(Method method, AITool annotation) {
        return annotation.name().isEmpty() ? method.getName() : annotation.name();
    }

    /**
     * 提取方法参数
     *
     * @param method 方法
     * @return 工具参数列表
     */
    private List<ToolParameter> extractParameters(Method method) {
        List<ToolParameter> parameters = new ArrayList<>();
        Parameter[] methodParams = method.getParameters();

        for (Parameter param : methodParams) {
            parameters.add(createToolParameter(param));
        }

        return parameters;
    }

    /**
     * 根据方法参数创建工具参数
     *
     * @param param 方法参数
     * @return 工具参数
     */
    private ToolParameter createToolParameter(Parameter param) {
        AIToolParameter annotation = param.getAnnotation(AIToolParameter.class);
        String name = param.getName();
        String description = "参数";
        String type = inferParamType(param.getType());
        boolean required = true;
        Object defaultValue = null;

        if (annotation != null) {
            if (!annotation.name().isEmpty()) {
                name = annotation.name();
            }
            description = annotation.description();
            if (!annotation.type().isEmpty()) {
                type = annotation.type();
            }
            required = annotation.required();
            if (!annotation.defaultValue().isEmpty()) {
                defaultValue = annotation.defaultValue();
            }
        }

        return ToolParameter.builder()
                .name(name)
                .type(type)
                .description(description)
                .required(required)
                .defaultValue(defaultValue)
                .build();
    }

    /**
     * 推断参数类型
     *
     * @param type Java类型
     * @return 参数类型字符串
     */
    private String inferParamType(Class<?> type) {
        if (type == String.class) {
            return "string";
        } else if (type == Integer.class || type == int.class || 
                  type == Long.class || type == long.class) {
            return "number";
        } else if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        } else if (type == Double.class || type == double.class ||
                  type == Float.class || type == float.class) {
            return "number";
        } else if (type.isArray() || List.class.isAssignableFrom(type)) {
            return "array";
        } else {
            return "object";
        }
    }
} 