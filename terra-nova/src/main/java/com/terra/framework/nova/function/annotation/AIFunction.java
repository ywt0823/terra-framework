package com.terra.framework.nova.function.annotation;

import java.lang.annotation.*;

/**
 * Annotation for marking methods that can be called by AI models.
 * Methods annotated with this will be automatically registered as available functions.
 *
 * @author terra
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AIFunction {
    /**
     * The name of the function. If not specified, will use the method name.
     *
     * @return function name
     */
    String name() default "";

    /**
     * Description of what the function does.
     *
     * @return function description
     */
    String description();

    /**
     * Category of the function for grouping and organization.
     *
     * @return function category
     */
    String category() default "default";
} 