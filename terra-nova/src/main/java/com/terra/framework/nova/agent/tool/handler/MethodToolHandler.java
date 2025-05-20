package com.terra.framework.nova.agent.tool.handler;

import com.terra.framework.nova.agent.tool.Tool;
import com.terra.framework.nova.agent.tool.ToolParameter;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 方法级工具处理程序
 * 将带注解的方法包装为Tool接口实现
 *
 * @author terra-nova
 */
@Slf4j
public class MethodToolHandler implements Tool {

    private final String name;
    private final String description;
    private final List<ToolParameter> parameters;
    private final Object target;
    private final Method method;
    private final Map<String, Integer> parameterIndexMap;

    /**
     * 构造函数
     *
     * @param name 工具名称
     * @param description 工具描述
     * @param parameters 参数列表
     * @param target 目标对象
     * @param method 方法
     */
    public MethodToolHandler(String name, String description, List<ToolParameter> parameters,
                          Object target, Method method) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.target = target;
        this.method = method;
        this.parameterIndexMap = buildParameterIndexMap(parameters);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getDescription() {
        return description;
    }

    @Override
    public List<ToolParameter> getParameters() {
        return parameters;
    }

    @Override
    public Object execute(Map<String, Object> parameters) {
        try {
            Object[] args = prepareArguments(parameters);
            return method.invoke(target, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.error("执行工具方法时出错: {}.{}", target.getClass().getName(), method.getName(), e);
            if (e.getCause() != null) {
                return "执行错误: " + e.getCause().getMessage();
            }
            return "执行错误: " + e.getMessage();
        } catch (Exception e) {
            log.error("调用工具时出现异常", e);
            return "调用异常: " + e.getMessage();
        }
    }

    /**
     * 构建参数索引映射
     *
     * @param toolParameters 工具参数列表
     * @return 参数名到索引的映射
     */
    private Map<String, Integer> buildParameterIndexMap(List<ToolParameter> toolParameters) {
        Map<String, Integer> indexMap = new ConcurrentHashMap<>();
        for (int i = 0; i < toolParameters.size(); i++) {
            indexMap.put(toolParameters.get(i).getName(), i);
        }
        return indexMap;
    }

    /**
     * 准备方法调用的参数
     *
     * @param parameters 参数映射
     * @return 参数数组
     */
    private Object[] prepareArguments(Map<String, Object> parameters) {
        Parameter[] methodParams = method.getParameters();
        Object[] args = new Object[methodParams.length];

        for (ToolParameter toolParam : this.parameters) {
            Integer index = parameterIndexMap.get(toolParam.getName());
            if (index != null && index < methodParams.length) {
                Object value = parameters.get(toolParam.getName());
                
                // 处理默认值
                if (value == null && !toolParam.isRequired() && toolParam.getDefaultValue() != null) {
                    value = convertDefaultValue(toolParam.getDefaultValue().toString(), methodParams[index].getType());
                }
                
                // 类型转换
                if (value != null) {
                    value = convertValueIfNeeded(value, methodParams[index].getType());
                }
                
                args[index] = value;
            }
        }

        return args;
    }

    /**
     * 根据方法参数类型进行必要的类型转换
     *
     * @param value 原始值
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private Object convertValueIfNeeded(Object value, Class<?> targetType) {
        if (value == null) {
            return null;
        }

        if (targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        try {
            if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(value.toString());
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(value.toString());
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(value.toString());
            } else if (targetType == float.class || targetType == Float.class) {
                return Float.parseFloat(value.toString());
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(value.toString());
            } else if (targetType == String.class) {
                return value.toString();
            }
        } catch (Exception e) {
            log.warn("参数转换失败: {} -> {}", value, targetType.getName(), e);
        }

        return value;
    }

    /**
     * 将默认值字符串转换为适当的类型
     *
     * @param defaultValue 默认值字符串
     * @param targetType 目标类型
     * @return 转换后的值
     */
    private Object convertDefaultValue(String defaultValue, Class<?> targetType) {
        if (defaultValue == null || defaultValue.isEmpty()) {
            return null;
        }

        try {
            if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(defaultValue);
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(defaultValue);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(defaultValue);
            } else if (targetType == float.class || targetType == Float.class) {
                return Float.parseFloat(defaultValue);
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                return Boolean.parseBoolean(defaultValue);
            } else if (targetType == String.class) {
                return defaultValue;
            }
        } catch (Exception e) {
            log.warn("默认值转换失败: {} -> {}", defaultValue, targetType.getName(), e);
        }

        return null;
    }
} 