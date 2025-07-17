package com.terra.framework.nova.prompt.scanner;

import com.terra.framework.nova.prompt.annotation.PromptMapper;
import com.terra.framework.nova.prompt.config.PromptMapperScanConfiguration;
import com.terra.framework.nova.prompt.factory.EnhancedPromptMapperFactoryBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition;
import org.springframework.beans.factory.config.BeanDefinitionHolder;
import org.springframework.beans.factory.support.AbstractBeanDefinition;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.annotation.ClassPathBeanDefinitionScanner;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.core.type.filter.AssignableTypeFilter;
import org.springframework.core.type.filter.TypeFilter;

import java.util.Arrays;
import java.util.Set;

/**
 * 增强的 PromptMapper 扫描器。
 * <p>
 * 这个类继承自 {@link ClassPathBeanDefinitionScanner}，专门用于扫描带有
 * {@link PromptMapper} 注解的接口，并将其注册为 Bean 定义。
 * <p>
 * 相对于原始的 {@link ClassPathPromptMapperScanner}，
 * 这个增强版本支持更灵活的配置，包括特定的模型绑定、配置隔离等功能。
 *
 * @author DeavyJones
 * @since 1.0.0
 */
public class EnhancedPromptMapperScanner extends ClassPathBeanDefinitionScanner {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedPromptMapperScanner.class);

    /**
     * 扫描配置
     */
    private PromptMapperScanConfiguration scanConfiguration;

    /**
     * 构造函数
     *
     * @param registry Bean 定义注册器
     */
    public EnhancedPromptMapperScanner(BeanDefinitionRegistry registry) {
        super(registry, false);
    }

    /**
     * 设置扫描配置
     *
     * @param scanConfiguration 扫描配置
     */
    public void setScanConfiguration(PromptMapperScanConfiguration scanConfiguration) {
        this.scanConfiguration = scanConfiguration;
    }

    /**
     * 注册过滤器
     */
    public void registerFilters() {
        if (scanConfiguration == null) {
            throw new IllegalStateException("Scan configuration must be set before registering filters");
        }

        // 添加包含过滤器
        addIncludeFilter(new AnnotationTypeFilter(scanConfiguration.getAnnotationClass()));

        // 添加排除过滤器
        if (scanConfiguration.getExcludeClasses() != null) {
            for (Class<?> excludeClass : scanConfiguration.getExcludeClasses()) {
                addExcludeFilter(new AssignableTypeFilter(excludeClass));
            }
        }

        // 排除 java.lang.annotation.Annotation 本身
        addExcludeFilter(new TypeFilter() {
            @Override
            public boolean match(org.springframework.core.type.classreading.MetadataReader metadataReader,
                                 org.springframework.core.type.classreading.MetadataReaderFactory metadataReaderFactory) {
                String className = metadataReader.getClassMetadata().getClassName();
                return className.endsWith("package-info");
            }
        });
    }

    /**
     * 执行扫描
     *
     * @param basePackages 基础包路径
     * @return 扫描到的 Bean 定义数量
     */
    public int scan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = doScan(basePackages);
        return beanDefinitions.size();
    }

    @Override
    protected Set<BeanDefinitionHolder> doScan(String... basePackages) {
        Set<BeanDefinitionHolder> beanDefinitions = super.doScan(basePackages);

        if (beanDefinitions.isEmpty()) {
            logger.warn("No PromptMapper interfaces found in packages: {}", Arrays.toString(basePackages));
        } else {
            processBeanDefinitions(beanDefinitions);
        }

        return beanDefinitions;
    }

    /**
     * 处理 Bean 定义
     *
     * @param beanDefinitions Bean 定义集合
     */
    private void processBeanDefinitions(Set<BeanDefinitionHolder> beanDefinitions) {
        for (BeanDefinitionHolder holder : beanDefinitions) {
            AbstractBeanDefinition definition = (AbstractBeanDefinition) holder.getBeanDefinition();
            String mapperClassName = definition.getBeanClassName();

            if (mapperClassName == null) {
                logger.warn("Bean definition has no class name: {}", holder.getBeanName());
                continue;
            }

            logger.debug("Processing PromptMapper interface: {}", mapperClassName);

            // 将 Bean 定义配置为 FactoryBean
            definition.setBeanClass(EnhancedPromptMapperFactoryBean.class);

            // 设置构造函数参数：接口类名
            definition.getConstructorArgumentValues().addGenericArgumentValue(mapperClassName);

            // 设置属性：扫描配置
            definition.getPropertyValues().add("scanConfiguration", scanConfiguration);

            // 设置自动装配模式
            definition.setAutowireMode(AbstractBeanDefinition.AUTOWIRE_BY_TYPE);

            // 设置为单例
            definition.setScope(AbstractBeanDefinition.SCOPE_SINGLETON);

            // 设置延迟初始化为 false，确保启动时就创建
            definition.setLazyInit(false);

            logger.debug("Configured PromptMapper bean: {} with configuration: {}",
                holder.getBeanName(), scanConfiguration.getConfigId());
        }
    }

    @Override
    protected boolean isCandidateComponent(AnnotatedBeanDefinition beanDefinition) {
        // 只接受接口类型的组件
        boolean isInterface = beanDefinition.getMetadata().isInterface();
        boolean isAbstract = beanDefinition.getMetadata().isAbstract();

        if (!isInterface) {
            logger.debug("Skipping non-interface class: {}", beanDefinition.getMetadata().getClassName());
            return false;
        }

        if (isAbstract && !isInterface) {
            logger.debug("Skipping abstract class: {}", beanDefinition.getMetadata().getClassName());
            return false;
        }

        return true;
    }

    /**
     * 检查候选组件是否可以注册
     *
     * @param beanName       Bean 名称
     * @param beanDefinition Bean 定义
     * @return 是否可以注册
     */
    protected boolean checkCandidate(String beanName, AbstractBeanDefinition beanDefinition) {
        // 检查是否已经存在相同的 Bean 定义
        if (getRegistry().containsBeanDefinition(beanName)) {
            logger.warn("Skipping PromptMapper interface {} because of existing bean definition with name: {}",
                beanDefinition.getBeanClassName(), beanName);
            return false;
        }
        return true;
    }
}
