package com.terra.framework.nova.function.adapter.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.terra.framework.nova.function.Function;
import com.terra.framework.nova.function.FunctionCall;
import com.terra.framework.nova.function.Parameter;
import com.terra.framework.nova.function.adapter.FunctionFormatAdapter;
import com.terra.framework.nova.llm.model.ModelResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认函数格式适配器实现
 *
 * @author terra-nova
 */
public class DefaultFunctionAdapter implements FunctionFormatAdapter {

    private static final Logger log = LoggerFactory.getLogger(DefaultFunctionAdapter.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Object formatFunctionsForModel(List<Function> functions) {
        List<Map<String, Object>> formattedFunctions = new ArrayList<>();

        for (Function function : functions) {
            Map<String, Object> functionMap = new HashMap<>();
            functionMap.put("name", function.getName());
            functionMap.put("description", function.getDescription());

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("type", "object");
            Map<String, Object> properties = new HashMap<>();
            List<String> required = new ArrayList<>();

            for (Parameter parameter : function.getParameters()) {
                properties.put(parameter.getName(), parameter.getSchema().toJsonSchema());
                if (parameter.isRequired()) {
                    required.add(parameter.getName());
                }
            }

            parameters.put("properties", properties);
            if (!required.isEmpty()) {
                parameters.put("required", required);
            }

            functionMap.put("parameters", parameters);
            formattedFunctions.add(functionMap);
        }

        return formattedFunctions;
    }

    @Override
    public FunctionCall parseFunctionCallFromResponse(ModelResponse response) {
        try {
            String functionName = getFunctionNameFromResponse(response);
            if (functionName == null) {
                return null;
            }

            Map<String, Object> arguments = getArgumentsFromResponse(response);
            return new FunctionCall(functionName, arguments);
        } catch (Exception e) {
            log.error("Failed to parse function call from response", e);
            return null;
        }
    }

    /**
     * 从响应中获取函数名称
     *
     * @param response 模型响应
     * @return 函数名称，如果没有则返回null
     */
    protected String getFunctionNameFromResponse(ModelResponse response) {
        // 由具体子类实现具体的解析逻辑
        return null;
    }

    /**
     * 从响应中获取函数参数
     *
     * @param response 模型响应
     * @return 函数参数
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getArgumentsFromResponse(ModelResponse response) {
        // 由具体子类实现具体的解析逻辑
        return new HashMap<>();
    }
}
