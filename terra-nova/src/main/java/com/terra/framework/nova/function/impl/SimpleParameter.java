package com.terra.framework.nova.function.impl;

import com.terra.framework.nova.function.Parameter;
import com.terra.framework.nova.function.Schema;

import java.util.Objects;

/**
 * 简单参数实现
 *
 * @author terra-nova
 */
public class SimpleParameter implements Parameter {
    
    private final String name;
    private final String description;
    private final Schema schema;
    private final boolean required;
    
    /**
     * 构造函数
     *
     * @param name 参数名称
     * @param description 参数描述
     * @param schema 参数模式
     * @param required 是否必需
     */
    public SimpleParameter(String name, String description, Schema schema, boolean required) {
        this.name = Objects.requireNonNull(name, "Parameter name cannot be null");
        this.description = description;
        this.schema = Objects.requireNonNull(schema, "Parameter schema cannot be null");
        this.required = required;
    }
    
    @Override
    public String getName() {
        return name;
    }
    
    @Override
    public String getDescription() {
        return description;
    }
    
    @Override
    public Schema getSchema() {
        return schema;
    }
    
    @Override
    public boolean isRequired() {
        return required;
    }
} 