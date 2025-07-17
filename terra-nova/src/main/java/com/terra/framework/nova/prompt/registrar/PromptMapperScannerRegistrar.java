package com.terra.framework.nova.prompt.registrar;

import com.terra.framework.nova.prompt.annotation.PromptMapperScan;
import com.terra.framework.nova.prompt.annotation.PromptMapperScans;
import com.terra.framework.nova.prompt.config.PromptConfig;
import com.terra.framework.nova.prompt.config.PromptMapperScanConfiguration;
import com.terra.framework.nova.prompt.scanner.EnhancedPromptMapperScanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.context.ResourceLoaderAware;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.annotation.AnnotationAttributes;
import org.springframework.core.io.ResourceLoader;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 处理 {@link PromptMapperScan} 注解的注册器。
 * <p>
 * 这个类负责解析 {@link PromptMapperScan} 注解，并根据配置启动相应的扫描器
 * 来注册 PromptMapper 接口的 Bean 定义。
 *
 * @author DeavyJones
 * @since 1.0.0
 */
public class PromptMapperScannerRegistrar implements ImportBeanDefinitionRegistrar, ResourceLoaderAware {

    // Note: Do not move resourceLoader via cleanup
    private ResourceLoader resourceLoader;

    private static final Logger logger = LoggerFactory.getLogger(PromptMapperScannerRegistrar.class);

    @Override
    public void registerBeanDefinitions(AnnotationMetadata importingClassMetadata, BeanDefinitionRegistry registry) {
        List<PromptMapperScanConfiguration> scanConfigurations = parseScanConfigurations(importingClassMetadata);

        if (scanConfigurations.isEmpty()) {
            logger.warn("No PromptMapperScan configurations found in {}", importingClassMetadata.getClassName());
            return;
        }

        for (PromptMapperScanConfiguration config : scanConfigurations) {
            try {
                config.validate();
                registerPromptMappers(config, registry);
                logger.info("Successfully registered PromptMapper scan configuration: {}", config.getConfigId());
            } catch (Exception e) {
                logger.error("Failed to register PromptMapper scan configuration: {}", config.getConfigId(), e);
                throw new IllegalStateException("Failed to register PromptMapper scan configuration", e);
            }
        }
    }

    /**
     * 从注解元数据中解析扫描配置
     *
     * @param metadata 注解元数据
     * @return 扫描配置列表
     */
    private List<PromptMapperScanConfiguration> parseScanConfigurations(AnnotationMetadata metadata) {
        List<PromptMapperScanConfiguration> configurations = new ArrayList<>();

        // 处理单个 @PromptMapperScan 注解
        AnnotationAttributes attributes = AnnotationAttributes.fromMap(
            metadata.getAnnotationAttributes(PromptMapperScan.class.getName())
        );
        if (attributes != null) {
            configurations.add(parseSingleConfiguration(attributes, metadata));
        }

        // 处理多个 @PromptMapperScan 注解（通过 @PromptMapperScans 容器）
        AnnotationAttributes scanAttributes = AnnotationAttributes.fromMap(
            metadata.getAnnotationAttributes(PromptMapperScans.class.getName())
        );
        if (scanAttributes != null) {
            AnnotationAttributes[] scanArray = scanAttributes.getAnnotationArray("value");
            for (AnnotationAttributes scanAttribute : scanArray) {
                configurations.add(parseSingleConfiguration(scanAttribute, metadata));
            }
        }

        return configurations;
    }

    /**
     * 解析单个扫描配置
     *
     * @param attributes 注解属性
     * @param metadata   注解元数据
     * @return 扫描配置
     */
    private PromptMapperScanConfiguration parseSingleConfiguration(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        PromptMapperScanConfiguration config = new PromptMapperScanConfiguration();

        // 解析基础包路径
        String[] basePackages = parseBasePackages(attributes, metadata);
        config.setBasePackages(basePackages);

        // 解析 ChatModel 配置
        String chatModelBeanName = attributes.getString("chatModel");
        if (StringUtils.hasText(chatModelBeanName)) {
            config.setChatModelBeanName(chatModelBeanName);
        }

        Class<? extends ChatModel> chatModelClass = attributes.getClass("chatModelClass");
        if (chatModelClass != ChatModel.class) {
            config.setChatModelClass(chatModelClass);
        }

        // 解析默认 Prompt 配置
        PromptConfig defaultConfig = parseDefaultConfig(attributes);
        config.setDefaultConfig(defaultConfig);

        // 解析注解类型
        Class<? extends Annotation> annotationClass = attributes.getClass("annotationClass");
        config.setAnnotationClass(annotationClass);

        // 解析排除的类型
        Class<?>[] excludeClasses = attributes.getClassArray("excludeClasses");
        config.setExcludeClasses(excludeClasses);

        // 解析配置ID
        String configId = attributes.getString("configId");
        if (StringUtils.hasText(configId)) {
            config.setConfigId(configId);
        }

        return config;
    }

    /**
     * 解析基础包路径
     *
     * @param attributes 注解属性
     * @param metadata   注解元数据
     * @return 基础包路径数组
     */
    private String[] parseBasePackages(AnnotationAttributes attributes, AnnotationMetadata metadata) {
        List<String> basePackages = new ArrayList<>();

        // 从 value 属性获取
        String[] valuePackages = attributes.getStringArray("value");
        if (valuePackages.length > 0) {
            basePackages.addAll(Arrays.asList(valuePackages));
        }

        // 从 basePackages 属性获取
        String[] basePackageArray = attributes.getStringArray("basePackages");
        if (basePackageArray.length > 0) {
            basePackages.addAll(Arrays.asList(basePackageArray));
        }

        // 从 basePackageClasses 属性获取
        Class<?>[] basePackageClasses = attributes.getClassArray("basePackageClasses");
        for (Class<?> clazz : basePackageClasses) {
            basePackages.add(ClassUtils.getPackageName(clazz));
        }

        // 如果没有指定包路径，使用导入类的包
        if (basePackages.isEmpty()) {
            String importingClassName = metadata.getClassName();
            String packageName = ClassUtils.getPackageName(importingClassName);
            basePackages.add(packageName);
        }

        return basePackages.toArray(new String[0]);
    }

    /**
     * 解析默认 Prompt 配置
     *
     * @param attributes 注解属性
     * @return 默认 Prompt 配置
     */
    private PromptConfig parseDefaultConfig(AnnotationAttributes attributes) {
        PromptConfig config = new PromptConfig();

        // 解析模型名称
        String modelName = attributes.getString("modelName");
        if (StringUtils.hasText(modelName)) {
            config.setModel(modelName);
        }

        // 解析温度
        double temperature = attributes.getNumber("temperature").doubleValue();
        if (temperature >= 0) {
            config.setTemperature(temperature);
        }

        // 解析最大 token 数
        int maxTokens = attributes.getNumber("maxTokens").intValue();
        if (maxTokens > 0) {
            config.setMaxTokens(maxTokens);
        }

        // 解析 top-p
        double topP = attributes.getNumber("topP").doubleValue();
        if (topP >= 0) {
            config.setTopP(topP);
        }

        return config;
    }

    /**
     * 注册 PromptMapper 接口
     *
     * @param config   扫描配置
     * @param registry Bean 定义注册器
     */
    private void registerPromptMappers(PromptMapperScanConfiguration config, BeanDefinitionRegistry registry) {
        EnhancedPromptMapperScanner scanner = new EnhancedPromptMapperScanner(registry);
        scanner.setScanConfiguration(config);
        scanner.registerFilters();

        String[] basePackages = config.getBasePackages();
        logger.debug("Scanning packages: {} for PromptMapper interfaces", Arrays.toString(basePackages));

        int beanCount = scanner.scan(basePackages);
        logger.info("Registered {} PromptMapper beans from packages: {}", beanCount, Arrays.toString(basePackages));
    }

    @Override
    public void setResourceLoader(ResourceLoader resourceLoader) {
        this.resourceLoader = resourceLoader;
    }
}
