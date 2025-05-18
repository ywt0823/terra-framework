package com.terra.framework.nova.function.adapter;

import com.terra.framework.nova.function.Function;
import com.terra.framework.nova.function.FunctionCall;
import com.terra.framework.nova.llm.model.ModelResponse;

import java.util.List;

/**
 * 函数格式适配器接口，用于不同大模型厂商的函数格式转换
 *
 * @author terra-nova
 */
public interface FunctionFormatAdapter {

    /**
     * 将函数列表格式化为模型可接受的格式
     *
     * @param functions 函数列表
     * @return 格式化后的对象
     */
    Object formatFunctionsForModel(List<Function> functions);

    /**
     * 从模型响应中解析函数调用
     *
     * @param response 模型响应
     * @return 函数调用，如果没有则返回null
     */
    FunctionCall parseFunctionCallFromResponse(ModelResponse response);
}
