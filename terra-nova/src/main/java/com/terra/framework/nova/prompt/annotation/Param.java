package com.terra.framework.nova.prompt.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Binds a method parameter to a placeholder in the prompt template.
 * <p>
 * This annotation is used to map method parameters to named placeholders
 * (e.g., {@code ${placeholderName}}) within the XML prompt template.
 *
 * @author DeavyJones
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.PARAMETER)
public @interface Param {

    /**
     * The name of the placeholder in the prompt template.
     *
     * @return the placeholder name
     */
    String value();
} 