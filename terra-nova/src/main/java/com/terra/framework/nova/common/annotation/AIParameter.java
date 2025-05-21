package com.terra.framework.nova.common.annotation;

import java.lang.annotation.*;

/**
 * 标记AI组件的参数
 *
 * @author terra-nova
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AIParameter {
    /**
     * 参数名称
     * 如果未指定，将使用方法参数名
     *
     * @return 参数名称
     */
    String name() default "";
    
    /**
     * 参数描述
     *
     * @return 参数描述
     */
    String description();
    
    /**
     * 参数类型
     * 如果未指定，将根据Java类型推断
     *
     * @return 参数类型
     */
    String type() default "";
    
    /**
     * 是否为必需参数
     *
     * @return 是否必需
     */
    boolean required() default true;
    
    /**
     * 默认值
     * 仅用于非必需参数
     *
     * @return 默认值
     */
    String defaultValue() default "";
    
    /**
     * 可选值列表
     * 仅在Function模式下使用
     *
     * @return 可选值列表
     */
    String[] enumValues() default {};
    
    /**
     * 参数验证规则（JSON Schema格式）
     * 仅在Function模式下使用
     *
     * @return 验证规则
     */
    String[] validationRules() default {};
} 