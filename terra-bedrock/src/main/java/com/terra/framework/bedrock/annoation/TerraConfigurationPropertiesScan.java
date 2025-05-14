package com.terra.framework.bedrock.annoation;

import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author ywt
 * @description terra-boot扫描properties注解
 * @date 2025 年 5 月 14 日 15:43:16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@ConfigurationPropertiesScan(basePackages = "com.terra.*")
public @interface TerraConfigurationPropertiesScan {

    @AliasFor(attribute = "value", annotation = ConfigurationPropertiesScan.class)
    String[] value() default {
            "com.terra.*"
    };

    @AliasFor(attribute = "basePackages", annotation = ConfigurationPropertiesScan.class)
    String[] basePackages() default {
            "com.terra.*"
    };

    @AliasFor(attribute = "basePackageClasses", annotation = ConfigurationPropertiesScan.class)
    Class<?>[] basePackageClasses() default {};
}
