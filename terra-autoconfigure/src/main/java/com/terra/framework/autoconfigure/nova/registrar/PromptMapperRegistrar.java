package com.terra.framework.autoconfigure.nova.registrar;

import lombok.Setter;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.EnvironmentAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.env.Environment;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.StringUtils;

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
@Setter
public class PromptMapperRegistrar implements BeanFactoryAware, EnvironmentAware, ImportBeanDefinitionRegistrar {


    private BeanFactory beanFactory;
    private Environment environment;

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
        List<String> packages = AutoConfigurationPackages.get(this.beanFactory);
        basePackages.add(StringUtils.collectionToCommaDelimitedString(packages));

        return basePackages;
    }


    private String getBeanNameForType(Class<?> type, ListableBeanFactory factory) {
        String[] beanNames = factory.getBeanNamesForType(type);
        return beanNames.length > 0 ? beanNames[0] : null;
    }
}
