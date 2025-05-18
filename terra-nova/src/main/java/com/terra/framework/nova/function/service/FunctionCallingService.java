package com.terra.framework.nova.function.service;

import com.terra.framework.nova.function.Function;
import com.terra.framework.nova.function.FunctionCall;
import com.terra.framework.nova.llm.model.ModelRequest;
import com.terra.framework.nova.llm.model.ModelResponse;

import java.util.List;

/**
 * 函数调用服务接口
 *
 * @author terra-nova
 */
public interface FunctionCallingService {

    /**
     * 获取指定模型可用的函数列表
     *
     * @param modelId 模型ID
     * @return 函数列表
     */
    List<Function> getFunctionsForModel(String modelId);

    /**
     * 使用函数执行请求
     *
     * @param request 模型请求
     * @param functions 函数列表
     * @return 模型响应
     */
    ModelResponse executeWithFunctions(ModelRequest request, List<Function> functions);

    /**
     * 执行函数调用
     *
     * @param functionCall 函数调用
     * @return 函数执行结果
     */
    Object executeFunctionCall(FunctionCall functionCall);

    /**
     * 使用指定模型和函数列表执行请求
     *
     * @param request 模型请求
     * @param modelId 模型ID
     * @param functions 函数列表
     * @return 模型响应
     */
    ModelResponse executeWithFunctions(ModelRequest request, String modelId, List<Function> functions);
}
