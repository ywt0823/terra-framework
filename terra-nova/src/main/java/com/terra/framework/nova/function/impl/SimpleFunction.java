package com.terra.framework.nova.function.impl;

import com.terra.framework.nova.function.Function;
import com.terra.framework.nova.function.FunctionHandler;
import com.terra.framework.nova.function.Parameter;
import com.terra.framework.nova.function.Schema;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * 简单函数实现
 *
 * @author terra-nova
 */
@Getter
public class SimpleFunction implements Function {
    
    private final String name;
    private final String description;
    private final List<Parameter> parameters;
    private final Schema responseSchema;
    private final FunctionHandler handler;
    
    /**
     * 构造函数
     *
     * @param name 函数名称
     * @param description 函数描述
     * @param parameters 参数列表
     * @param responseSchema 返回值Schema
     * @param handler 函数处理器
     */
    private SimpleFunction(String name, String description, List<Parameter> parameters, 
                         Schema responseSchema, FunctionHandler handler) {
        this.name = Objects.requireNonNull(name, "Function name cannot be null");
        this.description = description;
        this.parameters = parameters != null ? 
                Collections.unmodifiableList(new ArrayList<>(parameters)) : 
                Collections.emptyList();
        this.responseSchema = responseSchema;
        this.handler = handler;
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
    public List<Parameter> getParameters() {
        return parameters;
    }
    
    @Override
    public Schema getResponseSchema() {
        return responseSchema;
    }
    
    public FunctionHandler getHandler() {
        return handler;
    }
    
    /**
     * 创建构建器
     *
     * @param name 函数名称
     * @return 构建器
     */
    public static Builder builder(String name) {
        return new Builder(name);
    }
    
    /**
     * 函数构建器
     */
    public static class Builder {
        private final String name;
        private String description;
        private final List<Parameter> parameters = new ArrayList<>();
        private Schema responseSchema;
        private FunctionHandler handler;
        
        /**
         * 构造函数
         *
         * @param name 函数名称
         */
        public Builder(String name) {
            this.name = Objects.requireNonNull(name, "Function name cannot be null");
        }
        
        /**
         * 设置描述
         *
         * @param description 函数描述
         * @return 构建器
         */
        public Builder description(String description) {
            this.description = description;
            return this;
        }
        
        /**
         * 添加参数
         *
         * @param parameter 参数
         * @return 构建器
         */
        public Builder addParameter(Parameter parameter) {
            this.parameters.add(parameter);
            return this;
        }
        
        /**
         * 添加多个参数
         *
         * @param parameters 参数列表
         * @return 构建器
         */
        public Builder addParameters(List<Parameter> parameters) {
            this.parameters.addAll(parameters);
            return this;
        }
        
        /**
         * 设置返回值Schema
         *
         * @param responseSchema 返回值Schema
         * @return 构建器
         */
        public Builder responseSchema(Schema responseSchema) {
            this.responseSchema = responseSchema;
            return this;
        }
        
        /**
         * 设置函数处理器
         *
         * @param handler 函数处理器
         * @return 构建器
         */
        public Builder handler(FunctionHandler handler) {
            this.handler = handler;
            return this;
        }
        
        /**
         * 构建函数
         *
         * @return 函数
         */
        public SimpleFunction build() {
            return new SimpleFunction(name, description, parameters, responseSchema, handler);
        }
    }
} 