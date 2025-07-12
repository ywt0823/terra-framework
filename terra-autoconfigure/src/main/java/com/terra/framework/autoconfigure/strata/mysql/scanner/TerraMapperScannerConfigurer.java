package com.terra.framework.autoconfigure.strata.mysql.scanner;

import com.terra.framework.strata.annoation.TerraDatasource;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.annotations.Mapper;
import org.mybatis.spring.mapper.MapperFactoryBean;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.boot.autoconfigure.AutoConfigurationPackages;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
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

        scanner.addIncludeFilter(new AnnotationTypeFilter(TerraDatasource.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(Mapper.class));

        List<String> basePackages = AutoConfigurationPackages.get(applicationContext.getAutowireCapableBeanFactory());

        for (String basePackage : basePackages) {
            for (BeanDefinition beanDefinition : scanner.findCandidateComponents(basePackage)) {
                if (beanDefinition instanceof AnnotatedBeanDefinition annotatedBeanDefinition) {

                    boolean hasMapperAnnotation = annotatedBeanDefinition.getMetadata().hasAnnotation(Mapper.class.getCanonicalName());
                    boolean hasTerraDatasourceAnnotation = annotatedBeanDefinition.getMetadata().hasAnnotation(TerraDatasource.class.getCanonicalName());

                    if (hasMapperAnnotation && hasTerraDatasourceAnnotation) {
                        Map<String, Object> annotationAttributes = annotatedBeanDefinition.getMetadata()
                            .getAnnotationAttributes(TerraDatasource.class.getCanonicalName());

                        if (annotationAttributes != null) {
                            String datasourceName = (String) annotationAttributes.get("datasourceName");
                            if (StringUtils.hasText(datasourceName)) {
                                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(MapperFactoryBean.class);
                                builder.addPropertyValue("mapperInterface", beanDefinition.getBeanClassName());
                                builder.addPropertyReference("sqlSessionTemplate", datasourceName + "SqlSessionTemplate");
                                builder.setLazyInit(false);

                                // 获取简单类名并转换为首字母小写的Bean名称
                                String className = beanDefinition.getBeanClassName();
                                String simpleName = Objects.requireNonNull(className).substring(className.lastIndexOf('.') + 1);
                                String beanName = Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);

                                registry.registerBeanDefinition(beanName, builder.getBeanDefinition());
                            }
                        }
                    } else {
                        if (hasMapperAnnotation) {
                            log.warn("警告：接口 {} 使用了 @Mapper 注解但缺少 @TerraDatasource 注解，将被忽略", beanDefinition.getBeanClassName());
                        }
                    }
                }
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // No-op
    }
}
