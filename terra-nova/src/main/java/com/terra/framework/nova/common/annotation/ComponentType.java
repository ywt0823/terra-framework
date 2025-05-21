package com.terra.framework.nova.common.annotation;

/**
 * AI组件类型，用于指定组件的使用场景
 *
 * @author terra-nova
 */
public enum ComponentType {
    /**
     * 作为Agent工具使用
     */
    TOOL,
    
    /**
     * 作为LLM函数调用使用
     */
    FUNCTION
} 