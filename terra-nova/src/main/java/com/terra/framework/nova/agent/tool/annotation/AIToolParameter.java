package com.terra.framework.nova.agent.tool.annotation;

import java.lang.annotation.*;

/**
 * 用于标记AI工具方法的参数
 *
 * @author terra-nova
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AIToolParameter {
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
     * 默认值，使用字符串表示
     * 仅用于非必需参数
     *
     * @return 默认值
     */
    String defaultValue() default "";
} 