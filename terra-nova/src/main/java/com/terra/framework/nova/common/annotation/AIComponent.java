package com.terra.framework.nova.common.annotation;

import java.lang.annotation.*;

/**
 * 标记一个方法作为AI组件，可同时用作Agent工具和LLM函数调用
 *
 * @author terra-nova
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AIComponent {
    /**
     * 组件名称
     * 如果未指定，将使用方法名
     *
     * @return 组件名称
     */
    String name() default "";
    
    /**
     * 组件描述
     *
     * @return 组件描述
     */
    String description();
    
    /**
     * 使用场景
     * 默认同时支持工具和函数调用场景
     *
     * @return 使用场景
     */
    ComponentType[] types() default {ComponentType.TOOL, ComponentType.FUNCTION};
    
    /**
     * 分类（主要用于Function）
     *
     * @return 分类
     */
    String category() default "default";
} 