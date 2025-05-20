package com.terra.framework.nova.agent.tool;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具参数
 *
 * @author terra-nova
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolParameter {
    
    /**
     * 参数名称
     */
    private String name;
    
    /**
     * 参数类型
     */
    private String type;
    
    /**
     * 参数描述
     */
    private String description;
    
    /**
     * 是否必需
     */
    private boolean required;
    
    /**
     * 默认值
     */
    private Object defaultValue;
    
    /**
     * 简化构造函数
     *
     * @param name 参数名称
     * @param type 参数类型
     * @param description 参数描述
     * @param required 是否必需
     */
    public ToolParameter(String name, String type, String description, boolean required) {
        this.name = name;
        this.type = type;
        this.description = description;
        this.required = required;
        this.defaultValue = null;
    }
} 