package com.terra.framework.nova.function.annotation;

import java.lang.annotation.*;

/**
 * Annotation for marking method parameters that need to be described for AI function calls.
 *
 * @author terra
 */
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface AIParameter {
    /**
     * The name of the parameter. If not specified, will use the parameter name from bytecode.
     *
     * @return parameter name
     */
    String name() default "";

    /**
     * Description of what the parameter is used for.
     *
     * @return parameter description
     */
    String description();

    /**
     * Whether this parameter is required.
     *
     * @return true if the parameter is required
     */
    boolean required() default true;

    /**
     * The schema type of the parameter.
     * If not specified, will be inferred from the parameter type.
     *
     * @return schema type
     */
    String type() default "";
} 