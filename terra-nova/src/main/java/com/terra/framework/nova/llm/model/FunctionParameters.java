package com.terra.framework.nova.llm.model;

import java.util.List;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * 函数参数的JSON Schema定义
 *
 * @author terra-nova
 */
@Data
@Builder
public class FunctionParameters {
    /**
     * 参数Schema类型，通常是 "object"
     */
    private String type;

    /**
     * 属性定义。键是参数名，值是对该参数的定义（包含类型、描述等）
     * 例如：{"location": {"type": "string", "description": "城市名称"}}
     */
    private Map<String, Object> properties;

    /**
     * 必需的参数名列表
     */
    private List<String> required;
} 