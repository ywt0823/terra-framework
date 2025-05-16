package com.terra.framework.nova.model.client;

/**
 * 流式响应处理接口
 *
 * @author terra-nova
 */
public interface StreamResponseHandler {
    
    /**
     * 处理单个数据块
     *
     * @param chunk 数据块
     */
    void onChunk(String chunk);
    
    /**
     * 响应完成时调用
     */
    void onComplete();
    
    /**
     * 发生错误时调用
     *
     * @param throwable 错误信息
     */
    void onError(Throwable throwable);
} 