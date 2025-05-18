package com.terra.framework.nova.function;

/**
 * 函数参数接口
 *
 * @author terra-nova
 */
public interface Parameter {
    
    /**
     * 获取参数名称
     *
     * @return 参数名称
     */
    String getName();
    
    /**
     * 获取参数描述
     *
     * @return 参数描述
     */
    String getDescription();
    
    /**
     * 获取参数模式 (Schema)
     *
     * @return 参数模式
     */
    Schema getSchema();
    
    /**
     * 是否为必需参数
     *
     * @return 是否必需
     */
    boolean isRequired();
} 