package com.terra.framework.nova.function.config;

import com.terra.framework.nova.function.FunctionExecutor;
import com.terra.framework.nova.function.FunctionRegistry;
import com.terra.framework.nova.function.adapter.FunctionFormatAdapter;
import com.terra.framework.nova.function.adapter.impl.DefaultFunctionAdapter;
import com.terra.framework.nova.function.impl.DefaultFunctionExecutor;
import com.terra.framework.nova.function.service.FunctionCallingService;
import com.terra.framework.nova.function.service.impl.DefaultFunctionCallingService;
import com.terra.framework.nova.llm.model.AIModelManager;
import com.terra.framework.nova.llm.service.AIService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 函数配置类
 *
 * @author terra-nova
 */
@Configuration
public class FunctionConfig {

    /**
     * 函数注册表
     *
     * @return 函数注册表
     */
    @Bean
    public FunctionRegistry functionRegistry() {
        return new FunctionRegistry();
    }

    /**
     * 函数执行器
     *
     * @return 函数执行器
     */
    @Bean
    public FunctionExecutor functionExecutor() {
        return new DefaultFunctionExecutor();
    }

    /**
     * 默认函数格式适配器
     *
     * @return 默认函数格式适配器
     */
    @Bean
    public FunctionFormatAdapter defaultFunctionAdapter() {
        return new DefaultFunctionAdapter();
    }

    /**
     * 函数调用服务
     *
     * @param functionRegistry 函数注册表
     * @param functionExecutor 函数执行器
     * @param aiService AI服务
     * @param modelManager 模型管理器
     * @return 函数调用服务
     */
    @Bean
    public FunctionCallingService functionCallingService(
            FunctionRegistry functionRegistry,
            FunctionExecutor functionExecutor,
            AIService aiService,
            AIModelManager modelManager,
            FunctionFormatAdapter defaultFunctionAdapter) {

        DefaultFunctionCallingService service = new DefaultFunctionCallingService(
                functionRegistry, functionExecutor, aiService, modelManager);

        // 注册默认适配器
        service.registerAdapter("default", defaultFunctionAdapter);

        return service;
    }
}
