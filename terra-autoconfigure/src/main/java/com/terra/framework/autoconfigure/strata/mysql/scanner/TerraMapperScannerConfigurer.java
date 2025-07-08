package com.terra.framework.autoconfigure.strata.mysql.scanner;

import com.terra.framework.strata.annoation.TerraMapper;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Objects;

public class TerraMapperScannerConfigurer implements BeanDefinitionRegistryPostProcessor, ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        ClassPathBeanDefinitionScanner scanner = new ClassPathBeanDefinitionScanner(registry, false) {
            @Override
            protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
                return beanDefinition.getMetadata().isInterface() && beanDefinition.getMetadata().isIndependent();
            }
        };

        scanner.addIncludeFilter(new AnnotationTypeFilter(TerraMapper.class));
        String[] basePackages = getBasePackages();
        for (String basePackage : basePackages) {
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(basePackage)) {
                if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {
                    Map<String, Object> annotationAttributes = annotatedBeanDefinition.getMetadata().getAnnotationAttributes(TerraMapper.class.getCanonicalName());
                    if (annotationAttributes != null) {
                        String datasourceName = (String) annotationAttributes.get("datasourceName");
                        if (StringUtils.hasText(datasourceName)) {
                            BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperFactoryBean.class);
                            builder.addPropertyValue("mapperInterface", beanDefinition.getBeanClassName());
                            builder.addPropertyReference("sqlSessionTemplate", datasourceName + "SqlSessionTemplate");
                            registry.registerBeanDefinition(Objects.requireNonNull(beanDefinition.getBeanClassName()), builder.getBeanDefinition());
                        }
                    }
                }
            }
        }
    }

    private String[] getBasePackages() {
        // Find packages to scan from the main application class @SpringBootApplication or a custom annotation
        // For simplicity, we scan from the package of the main application class.
        // A more robust solution might involve a custom annotation to specify scan packages.
        try {
            String mainClassName = Objects.requireNonNull(applicationContext.getEnvironment().getProperty("sun.java.command")).split(" ")[0];
            Class<?> mainClass = Class.forName(mainClassName);
            return new String[]{mainClass.getPackage().getName()};
        } catch (Exception e) {
            // Fallback or log a warning
            // This is a simple heuristic and might fail in some environments (e.g., when not started from a main method).
            // A more reliable way is needed, e.g., scanning all packages.
            // Let's scan from a known root, assuming a convention.
            // This part needs a better strategy. For now, we return a wide package.
            // A better implementation would be to let user configure this.
            return new String[]{"com.terra"}; // Default or configurable base package
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // No-op
    }
}
