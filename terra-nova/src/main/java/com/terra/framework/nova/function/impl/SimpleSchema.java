package com.terra.framework.nova.function.impl;

import com.terra.framework.nova.function.Schema;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 简单Schema实现
 *
 * @author terra-nova
 */
public class SimpleSchema implements Schema {
    
    private final String type;
    private final Map<String, Object> properties;
    
    /**
     * 构造函数
     *
     * @param type Schema类型
     */
    public SimpleSchema(String type) {
        this(type, Collections.emptyMap());
    }
    
    /**
     * 构造函数
     *
     * @param type Schema类型
     * @param properties Schema属性
     */
    public SimpleSchema(String type, Map<String, Object> properties) {
        this.type = Objects.requireNonNull(type, "Schema type cannot be null");
        this.properties = new HashMap<>(properties != null ? properties : Collections.emptyMap());
    }
    
    @Override
    public String getType() {
        return type;
    }
    
    @Override
    public Map<String, Object> toJsonSchema() {
        Map<String, Object> schema = new HashMap<>();
        schema.put("type", type);
        
        if (!properties.isEmpty()) {
            schema.put("properties", new HashMap<>(properties));
        }
        
        return schema;
    }
    
    /**
     * 创建字符串Schema
     *
     * @return 字符串Schema
     */
    public static SimpleSchema createStringSchema() {
        return new SimpleSchema("string");
    }
    
    /**
     * 创建数字Schema
     *
     * @return 数字Schema
     */
    public static SimpleSchema createNumberSchema() {
        return new SimpleSchema("number");
    }
    
    /**
     * 创建布尔Schema
     *
     * @return 布尔Schema
     */
    public static SimpleSchema createBooleanSchema() {
        return new SimpleSchema("boolean");
    }
    
    /**
     * 创建对象Schema
     *
     * @param properties 对象属性
     * @return 对象Schema
     */
    public static SimpleSchema createObjectSchema(Map<String, Object> properties) {
        return new SimpleSchema("object", properties);
    }
    
    /**
     * 创建数组Schema
     *
     * @param itemsSchema 数组元素Schema
     * @return 数组Schema
     */
    public static SimpleSchema createArraySchema(Schema itemsSchema) {
        Map<String, Object> properties = new HashMap<>();
        properties.put("items", itemsSchema.toJsonSchema());
        return new SimpleSchema("array", properties);
    }
} 