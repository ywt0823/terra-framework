package com.terra.framework.nova.prompt.annotation;

import org.springframework.stereotype.Repository;

import java.lang.annotation.*;

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
@Repository
public @interface PromptMapper {
}
