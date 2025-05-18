package com.terra.framework.nova.function;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 函数执行器接口
 *
 * @author terra-nova
 */
public interface FunctionExecutor {
    
    /**
     * 执行函数
     *
     * @param functionName 函数名称
     * @param parameters 函数参数
     * @return 函数执行结果
     */
    Object execute(String functionName, Map<String, Object> parameters);
    
    /**
     * 执行函数
     *
     * @param functionCall 函数调用
     * @return 函数执行结果
     */
    default Object execute(FunctionCall functionCall) {
        return execute(functionCall.getName(), functionCall.getArguments());
    }
    
    /**
     * 异步执行函数
     *
     * @param functionName 函数名称
     * @param parameters 函数参数
     * @return 异步函数执行结果
     */
    default CompletableFuture<Object> executeAsync(String functionName, Map<String, Object> parameters) {
        return CompletableFuture.supplyAsync(() -> execute(functionName, parameters));
    }
    
    /**
     * 异步执行函数
     *
     * @param functionCall 函数调用
     * @return 异步函数执行结果
     */
    default CompletableFuture<Object> executeAsync(FunctionCall functionCall) {
        return executeAsync(functionCall.getName(), functionCall.getArguments());
    }
}