package com.terra.framework.nova.function;

import java.util.List;

/**
 * 函数接口，用于定义可被模型调用的函数
 *
 * @author terra-nova
 */
public interface Function {
    
    /**
     * 获取函数名称
     *
     * @return 函数名称
     */
    String getName();
    
    /**
     * 获取函数描述
     *
     * @return 函数描述
     */
    String getDescription();
    
    /**
     * 获取函数参数列表
     *
     * @return 参数列表
     */
    List<Parameter> getParameters();
    
    /**
     * 获取返回值Schema
     *
     * @return 返回值Schema
     */
    Schema getResponseSchema();
} 