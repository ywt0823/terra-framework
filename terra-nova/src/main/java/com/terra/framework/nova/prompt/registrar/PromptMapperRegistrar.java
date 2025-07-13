package com.terra.framework.nova.prompt.registrar;

import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;

import java.util.ArrayList;
import java.util.List;

/**
 * Registrar to scan and register {@link com.terra.framework.nova.prompt.annotation.PromptMapper} interfaces.
 * <p>
 * This class is triggered by the auto-configuration and is responsible for
 * initiating the scan of prompt mapper interfaces. It automatically detects the
 * base package from the importing class configuration.
 *
 * @author DeavyJones
 */
public class PromptMapperRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        // Get the base packages to scan from the importing class
        List<String> basePackages = getBasePackages(importingClassMetadata);
        
        if (basePackages.isEmpty()) {
            return;
        }

        ClassPathPromptMapperScanner scanner = new ClassPathPromptMapperScanner(registry);
        scanner.registerFilters();
        scanner.doScan(basePackages.toArray(new String[0]));
    }

    private List<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        List<String> basePackages = new ArrayList<>();
        
        // 使用导入类的包作为基础包
        String importingClassName = importingClassMetadata.getClassName();
        String basePackage = getPackageName(importingClassName);
        basePackages.add(basePackage);
        
        return basePackages;
    }

    private String getPackageName(String className) {
        int lastDotIndex = className.lastIndexOf('.');
        return (lastDotIndex != -1) ? className.substring(0, lastDotIndex) : "";
    }
} 