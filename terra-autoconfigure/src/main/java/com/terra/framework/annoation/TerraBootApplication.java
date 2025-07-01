package com.terra.framework.annoation;

import org.springframework.beans.factory.support.BeanNameGenerator;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * @author ywt
 * @description terra-boot启动项注解
 * @date 2025 年 5 月 14 日 15:43:16
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@SpringBootApplication(scanBasePackages = "com.terra")
@TerraConfigurationPropertiesScan(basePackages = "com.terra.*")
public @interface TerraBootApplication {

    @AliasFor(annotation = EnableAutoConfiguration.class, attribute = "exclude")
    Class<?>[] exclude() default {};

    @AliasFor(annotation = ComponentScan.class, attribute = "excludeFilters")
    ComponentScan.Filter[] excludeFilters() default {};

    @AliasFor(annotation = ComponentScan.class, attribute = "basePackages")
    String[] scanComponentsBasePackages() default {"com.terra"};

    @AliasFor(annotation = ComponentScan.class, attribute = "basePackageClasses")
    Class<?>[] scanComponentsBasePackageClasses() default {};

    @AliasFor(annotation = ComponentScan.class, attribute = "nameGenerator")
    Class<? extends BeanNameGenerator> nameGenerator() default BeanNameGenerator.class;

    @AliasFor(annotation = Configuration.class)
    boolean proxyBeanMethods() default true;
}
