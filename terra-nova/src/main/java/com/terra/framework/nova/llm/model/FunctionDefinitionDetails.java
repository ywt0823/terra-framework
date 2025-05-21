package com.terra.framework.nova.llm.model;

import lombok.Builder;
import lombok.Data;

/**
 * 函数定义详情
 *
 * @author terra-nova
 */
@Data
@Builder
public class FunctionDefinitionDetails {
    /**
     * 函数名称
     */
    private String name;

    /**
     * 函数描述，用于帮助模型决定何时调用此函数
     */
    private String description;

    /**
     * 函数参数定义，以JSON Schema格式描述
     */
    private FunctionParameters parameters;
} 