package com.terra.framework.nova.function;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 函数注册表，管理所有可用的函数
 *
 * @author terra-nova
 */
public class FunctionRegistry {
    
    private static final Logger log = LoggerFactory.getLogger(FunctionRegistry.class);
    
    private final Map<String, Function> functions = new ConcurrentHashMap<>();
    
    /**
     * 注册函数
     *
     * @param function 函数
     */
    public void registerFunction(Function function) {
        functions.put(function.getName(), function);
        log.info("Registered function: {}", function.getName());
    }
    
    /**
     * 移除函数
     *
     * @param functionName 函数名称
     */
    public void removeFunction(String functionName) {
        functions.remove(functionName);
        log.info("Removed function: {}", functionName);
    }
    
    /**
     * 获取函数
     *
     * @param functionName 函数名称
     * @return 函数
     */
    public Function getFunction(String functionName) {
        Function function = functions.get(functionName);
        if (function == null) {
            throw new IllegalArgumentException("Function not found: " + functionName);
        }
        return function;
    }
    
    /**
     * 检查函数是否存在
     *
     * @param functionName 函数名称
     * @return 是否存在
     */
    public boolean hasFunction(String functionName) {
        return functions.containsKey(functionName);
    }
    
    /**
     * 获取所有函数
     *
     * @return 所有函数
     */
    public List<Function> getAllFunctions() {
        return new ArrayList<>(functions.values());
    }
    
    /**
     * 清除所有函数
     */
    public void clearFunctions() {
        functions.clear();
        log.info("Cleared all functions");
    }
} 