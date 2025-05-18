package com.terra.framework.nova.function.impl;

import com.terra.framework.nova.function.FunctionExecutor;
import com.terra.framework.nova.function.FunctionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.concurrent.CustomizableThreadFactory;

import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 默认函数执行器实现
 *
 * @author terra-nova
 */
public class DefaultFunctionExecutor implements FunctionExecutor {
    
    private static final Logger log = LoggerFactory.getLogger(DefaultFunctionExecutor.class);
    
    private final Map<String, FunctionHandler> handlers = new ConcurrentHashMap<>();
    
    /**
     * 异步执行线程池
     */
    private final Executor executor;
    
    /**
     * 构造函数
     */
    public DefaultFunctionExecutor() {
        this(Executors.newCachedThreadPool(new CustomizableThreadFactory("function-executor-")));
    }
    
    /**
     * 构造函数
     * 
     * @param executor 异步执行器
     */
    public DefaultFunctionExecutor(Executor executor) {
        this.executor = executor;
    }
    
    /**
     * 注册函数处理器
     *
     * @param functionName 函数名称
     * @param handler 处理器
     */
    public void registerHandler(String functionName, FunctionHandler handler) {
        handlers.put(functionName, handler);
        log.info("Registered function handler for: {}", functionName);
    }
    
    /**
     * 移除函数处理器
     *
     * @param functionName 函数名称
     */
    public void removeHandler(String functionName) {
        handlers.remove(functionName);
        log.info("Removed function handler for: {}", functionName);
    }
    
    @Override
    public Object execute(String functionName, Map<String, Object> parameters) {
        FunctionHandler handler = handlers.get(functionName);
        if (handler == null) {
            throw new IllegalArgumentException("No handler registered for function: " + functionName);
        }
        
        try {
            log.debug("Executing function: {} with parameters: {}", functionName, parameters);
            Object result = handler.handle(parameters);
            log.debug("Function execution completed: {}", functionName);
            return result;
        } catch (Exception e) {
            log.error("Error executing function: {}", functionName, e);
            throw new RuntimeException("Error executing function: " + functionName, e);
        }
    }
    
    @Override
    public CompletableFuture<Object> executeAsync(String functionName, Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> execute(functionName, parameters), executor);
    }
    
    /**
     * 检查是否存在函数处理器
     *
     * @param functionName 函数名称
     * @return 是否存在
     */
    public boolean hasHandler(String functionName) {
        return handlers.containsKey(functionName);
    }
    
    /**
     * 获取所有注册的函数名称
     *
     * @return 函数名称集合
     */
    public Iterable<String> getFunctionNames() {
        return handlers.keySet();
    }
} 