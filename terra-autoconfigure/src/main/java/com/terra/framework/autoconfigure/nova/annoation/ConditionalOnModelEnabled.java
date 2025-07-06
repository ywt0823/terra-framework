package com.terra.framework.autoconfigure.nova.annoation;

import com.terra.framework.autoconfigure.nova.config.condition.TerraModelEnabledCondition;
import org.springframework.context.annotation.Conditional;

import java.lang.annotation.*;

/**
 * @author Zeus
 * @date 2025年07月06日 15:34
 * @description ConditionalOnModelEnabled
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE, ElementType.METHOD})
@Documented
@Conditional(TerraModelEnabledCondition.class)
public @interface ConditionalOnModelEnabled {

    /**
     * 需要检查的模型名称（例如 "openai", "deepseek"）。
     *
     * @return 模型名称。
     */
    String value();

}
