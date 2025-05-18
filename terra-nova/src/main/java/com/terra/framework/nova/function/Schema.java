package com.terra.framework.nova.function;

import java.util.Map;

/**
 * Schema接口，用于描述参数或返回值的结构
 *
 * @author terra-nova
 */
public interface Schema {
    
    /**
     * 获取Schema类型
     *
     * @return Schema类型
     */
    String getType();
    
    /**
     * 转换为JSON Schema格式
     *
     * @return JSON Schema对象
     */
    Map<String, Object> toJsonSchema();
} 