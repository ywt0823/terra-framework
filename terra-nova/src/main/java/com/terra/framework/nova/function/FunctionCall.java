package com.terra.framework.nova.function;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 函数调用，表示模型请求执行的函数
 *
 * @author terra-nova
 */
public class FunctionCall {
    
    private final String name;
    private final Map<String, Object> arguments;
    
    /**
     * 构造函数
     *
     * @param name 函数名称
     * @param arguments 函数参数
     */
    public FunctionCall(String name, Map<String, Object> arguments) {
        this.name = Objects.requireNonNull(name, "Function name cannot be null");
        this.arguments = arguments != null ? 
                new HashMap<>(arguments) : Collections.emptyMap();
    }
    
    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    public String getName() {
        return name;
    }
    
    /**
     * 获取函数参数
     *
     * @return 函数参数
     */
    public Map<String, Object> getArguments() {
        return Collections.unmodifiableMap(arguments);
    }
    
    /**
     * 获取参数值
     *
     * @param name 参数名
     * @param <T> 参数类型
     * @return 参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T getArgument(String name) {
        return (T) arguments.get(name);
    }
    
    /**
     * 获取参数值，如果不存在则返回默认值
     *
     * @param name 参数名
     * @param defaultValue 默认值
     * @param <T> 参数类型
     * @return 参数值
     */
    @SuppressWarnings("unchecked")
    public <T> T getArgument(String name, T defaultValue) {
        return (T) arguments.getOrDefault(name, defaultValue);
    }
    
    @Override
    public String toString() {
        return "FunctionCall{" +
                "name='" + name + '\'' +
                ", arguments=" + arguments +
                '}';
    }
}