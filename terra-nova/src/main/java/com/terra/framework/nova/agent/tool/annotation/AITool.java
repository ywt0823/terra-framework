package com.terra.framework.nova.agent.tool.annotation;

import java.lang.annotation.*;

/**
 * 用于标记一个方法作为AI工具
 * 被此注解标记的方法将被自动注册为工具
 *
 * @author terra-nova
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AITool {
    /**
     * 工具名称
     * 如果未指定，将使用方法名
     *
     * @return 工具名称
     */
    String name() default "";

    /**
     * 工具描述
     *
     * @return 工具描述
     */
    String description();
} 