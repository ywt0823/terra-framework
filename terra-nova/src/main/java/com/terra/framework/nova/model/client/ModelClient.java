package com.terra.framework.nova.model.client;

import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * 模型客户端接口，定义与LLM模型交互的方法
 * 
 * @author terra-nova
 */
public interface ModelClient {
    
    /**
     * 同步调用LLM模型获取回复
     * 
     * @param prompt 提示词
     * @param parameters 模型参数
     * @return 模型生成的文本
     */
    String generate(String prompt, Map<String, Object> parameters);
    
    /**
     * 异步调用LLM模型获取回复
     * 
     * @param prompt 提示词
     * @param parameters 模型参数
     * @return 异步返回模型生成的文本
     */
    CompletableFuture<String> generateAsync(String prompt, Map<String, Object> parameters);
    
    /**
     * 流式调用LLM模型
     * 
     * @param prompt 提示词
     * @param parameters 模型参数
     * @param handler 流式响应处理器
     */
    void generateStream(String prompt, Map<String, Object> parameters, StreamResponseHandler handler);
    
    /**
     * 获取客户端支持的模型列表
     * 
     * @return 支持的模型列表
     */
    String[] getSupportedModels();
    
    /**
     * 获取客户端当前状态
     * 
     * @return 客户端状态
     */
    ClientStatus getStatus();
    
    /**
     * 初始化客户端
     */
    void init();
    
    /**
     * 关闭客户端
     */
    void close();
} 