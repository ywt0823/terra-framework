package com.terra.framework.nova.prompt.registrar;

import com.terra.framework.nova.prompt.annotation.PromptMapper;
import com.terra.framework.nova.prompt.factory.PromptMapperFactoryBean;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * A {@link ClassPathBeanDefinitionScanner} that scans for interfaces annotated
 * with {@link PromptMapper} and registers them as bean definitions.
 *
 * @author DeavyJones
 */
public class ClassPathPromptMapperScanner extends ClassPathBeanDefinitionScanner {

    public ClassPathPromptMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    /**
     * Configures the scanner to search for our {@link PromptMapper} annotation.
     */
    public void registerFilters() {
        addIncludeFilter(new AnnotationTypeFilter(PromptMapper.class));
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (!beanDefinitions.isEmpty()) {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        for (BeanDefinitionHolder holder : beanDefinitions) {
            AbstractBeanDefinition definition = (AbstractBeanDefinition) holder.getBeanDefinition();
            String mapperClassName = definition.getBeanClassName();
            if (mapperClassName == null) {
                continue;
            }

            // The bean definition is an interface, so we configure it to be a FactoryBean
            definition.setBeanClass(PromptMapperFactoryBean.class);
            definition.getConstructorArgumentValues().addGenericArgumentValue(mapperClassName);
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);
        }
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        // We only care about interfaces
        return beanDefinition.getMetadata().isInterface();
    }
} 