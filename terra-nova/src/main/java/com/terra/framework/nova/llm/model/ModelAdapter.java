package com.terra.framework.nova.llm.model;

import com.terra.framework.nova.llm.exception.ModelException;

/**
 * 模型适配器接口
 *
 * @author terra-nova
 */
public interface ModelAdapter {

    /**
     * 将通用请求转换为厂商特定请求
     *
     * @param <T> 厂商请求类型
     * @param request 通用请求
     * @param vendorRequestType 厂商请求类型
     * @return 厂商特定请求
     */
    <T> T convertRequest(ModelRequest request, Class<T> vendorRequestType);

    /**
     * 将厂商响应转换为通用响应
     *
     * @param <T> 厂商响应类型
     * @param vendorResponse 厂商响应
     * @return 通用响应
     */
    <T> ModelResponse convertResponse(T vendorResponse);

    /**
     * 处理厂商特定异常
     *
     * @param vendorException 厂商异常
     * @return 模型异常
     */
    ModelException handleException(Exception vendorException);
}
