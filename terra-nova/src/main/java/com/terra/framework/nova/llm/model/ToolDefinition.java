package com.terra.framework.nova.llm.model;

import lombok.Builder;
import lombok.Data;

/**
 * 工具定义，描述模型可以调用的工具
 * 目前仅支持"function"类型的工具
 *
 * @author terra-nova
 */
@Data
@Builder
public class ToolDefinition {
    /**
     * 工具类型，目前仅支持"function"
     */
    private String type;

    /**
     * 函数定义详情，当type为"function"时使用
     */
    private FunctionDefinitionDetails function;
    
    /**
     * 创建函数类型的工具定义
     *
     * @param function 函数定义详情
     * @return 工具定义
     */
    public static ToolDefinition ofFunction(FunctionDefinitionDetails function) {
        return ToolDefinition.builder()
                .type("function")
                .function(function)
                .build();
    }
} 