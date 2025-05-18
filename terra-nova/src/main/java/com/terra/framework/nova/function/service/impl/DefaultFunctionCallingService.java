package com.terra.framework.nova.function.service.impl;

import com.terra.framework.nova.function.Function;
import com.terra.framework.nova.function.FunctionCall;
import com.terra.framework.nova.function.FunctionExecutor;
import com.terra.framework.nova.function.FunctionRegistry;
import com.terra.framework.nova.function.adapter.FunctionFormatAdapter;
import com.terra.framework.nova.function.service.FunctionCallingService;
import com.terra.framework.nova.llm.model.*;
import com.terra.framework.nova.llm.service.AIService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 默认函数调用服务实现
 *
 * @author terra-nova
 */
@Service
public class DefaultFunctionCallingService implements FunctionCallingService {

    private static final Logger log = LoggerFactory.getLogger(DefaultFunctionCallingService.class);

    private final FunctionRegistry functionRegistry;
    private final FunctionExecutor functionExecutor;
    private final AIService aiService;
    private final AIModelManager modelManager;
    private final Map<String, FunctionFormatAdapter> adapterMap = new HashMap<>();

    /**
     * 构造函数
     *
     * @param functionRegistry 函数注册表
     * @param functionExecutor 函数执行器
     * @param aiService        AI服务
     * @param modelManager     模型管理器
     */
    @Autowired
    public DefaultFunctionCallingService(
        FunctionRegistry functionRegistry,
        FunctionExecutor functionExecutor,
        AIService aiService,
        AIModelManager modelManager) {
        this.functionRegistry = functionRegistry;
        this.functionExecutor = functionExecutor;
        this.aiService = aiService;
        this.modelManager = modelManager;
    }

    /**
     * 注册适配器
     *
     * @param provider 模型提供商
     * @param adapter  适配器
     */
    public void registerAdapter(String provider, FunctionFormatAdapter adapter) {
        adapterMap.put(provider, adapter);
        log.info("Registered function format adapter for provider: {}", provider);
    }

    @Override
    public List<Function> getFunctionsForModel(String modelId) {
        // 这里可以根据模型ID进行筛选，返回适合该模型的函数
        // 简单实现返回所有函数
        return functionRegistry.getAllFunctions();
    }

    @Override
    public ModelResponse executeWithFunctions(ModelRequest request, List<Function> functions) {
        // 从参数中获取模型ID
        Map<String, Object> parameters = request.getParameters();
        String modelId = (String) parameters.getOrDefault("modelId", "default");
        return executeWithFunctions(request, modelId, functions);
    }

    @Override
    public ModelResponse executeWithFunctions(ModelRequest request, String modelId, List<Function> functions) {
        AIModel model = modelManager.getModel(modelId);
        ModelInfo modelInfo = model.getModelInfo();
        String vendor = modelInfo.getVendor();

        // 获取适配器
        FunctionFormatAdapter adapter = getAdapterForProvider(vendor);

        // 格式化函数
        Object formattedFunctions = adapter.formatFunctionsForModel(functions);

        // 将函数添加到请求中
        Map<String, Object> parameters = new HashMap<>(request.getParameters());
        parameters.put("functions", formattedFunctions);

        // 克隆请求并添加函数
        ModelRequest requestWithFunctions = ModelRequest.builder()
            .withPrompt(request.getPrompt())
            .addMessages(request.getMessages())
            .withParameters(parameters)
            .build();

        // 执行请求
        ModelResponse response = aiService.generateResponse(requestWithFunctions.getPrompt(), requestWithFunctions.getParameters());

        // 检查是否有函数调用
        FunctionCall functionCall = adapter.parseFunctionCallFromResponse(response);
        if (functionCall != null) {
            log.info("Function call detected: {}", functionCall.getName());

            // 执行函数
            Object result = executeFunctionCall(functionCall);

            // 将结果添加到响应中
            // 这里简单处理，实际中可能需要根据特定模型进行后续对话
            Map<String, Object> functionResult = new HashMap<>();
            functionResult.put("name", functionCall.getName());
            functionResult.put("result", result);

            // 将函数结果添加到响应的metadata中
            Map<String, Object> rawResponse = response.getRawResponse();
            if (rawResponse == null) {
                rawResponse = new HashMap<>();
            }
            rawResponse.put("function_result", functionResult);

            // 创建新的响应对象
            ModelResponse newResponse = new ModelResponse();
            newResponse.setContent(response.getContent());
            newResponse.setModelId(response.getModelId());
            newResponse.setTokenUsage(response.getTokenUsage());
            newResponse.setRawResponse(rawResponse);

            return newResponse;
        }

        return response;
    }

    @Override
    public Object executeFunctionCall(FunctionCall functionCall) {
        return functionExecutor.execute(functionCall);
    }

    /**
     * 根据提供商获取适配器
     *
     * @param provider 提供商
     * @return 适配器
     */
    protected FunctionFormatAdapter getAdapterForProvider(String provider) {
        FunctionFormatAdapter adapter = adapterMap.get(provider);
        if (adapter == null) {
            log.warn("No adapter found for provider: {}, using default adapter", provider);
            adapter = adapterMap.get("default");
            if (adapter == null) {
                throw new IllegalStateException("No default adapter available");
            }
        }
        return adapter;
    }
}
