package com.terra.framework.nova.prompt.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A marker annotation to identify interfaces as Prompt Mappers.
 * <p>
 * Any interface annotated with {@code @PromptMapper} will be scanned and registered
 * as a Spring bean, with a proxy implementation that executes prompts against an AI model.
 *
 * @author DeavyJones
 */
@Documented
@Inherited
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface PromptMapper {
} 