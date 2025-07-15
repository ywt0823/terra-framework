package com.terra.framework.nova.prompt.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.util.StringUtils;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Objects;
import java.util.UUID;

/**
 * PromptMapper 扫描配置类。
 * <p>
 * 这个类封装了从 {@link com.terra.framework.nova.prompt.annotation.PromptMapperScan}
 * 注解解析出的配置信息，包括扫描包路径、模型配置、默认参数等。
 *
 * @author DeavyJones
 * @since 1.0.0
 */
@Setter
@Getter
public class PromptMapperScanConfiguration {

    /**
     * 扫描的基础包路径
     */
    private String[] basePackages;

    /**
     * 指定的 ChatModel Bean 名称
     */
    private String chatModelBeanName;

    /**
     * 指定的 ChatModel Bean 类型
     */
    private Class<? extends ChatModel> chatModelClass;

    /**
     * 默认的 Prompt 配置
     */
    private PromptConfig defaultConfig;

    /**
     * 要扫描的注解类型
     */
    private Class<? extends Annotation> annotationClass;

    /**
     * 排除的类型
     */
    private Class<?>[] excludeClasses;

    /**
     * 配置的唯一标识符
     */
    private String configId;

    /**
     * 默认构造函数
     */
    public PromptMapperScanConfiguration() {
        this.defaultConfig = new PromptConfig();
        this.chatModelClass = ChatModel.class;
        this.excludeClasses = new Class<?>[0];
        this.configId = UUID.randomUUID().toString();
    }

    /**
     * 完整构造函数
     *
     * @param basePackages      基础包路径
     * @param chatModelBeanName ChatModel Bean 名称
     * @param chatModelClass    ChatModel Bean 类型
     * @param defaultConfig     默认配置
     * @param annotationClass   注解类型
     * @param excludeClasses    排除的类型
     * @param configId          配置标识符
     */
    public PromptMapperScanConfiguration(String[] basePackages,
                                         String chatModelBeanName,
                                         Class<? extends ChatModel> chatModelClass,
                                         PromptConfig defaultConfig,
                                         Class<? extends Annotation> annotationClass,
                                         Class<?>[] excludeClasses,
                                         String configId) {
        this.basePackages = basePackages;
        this.chatModelBeanName = chatModelBeanName;
        this.chatModelClass = chatModelClass;
        this.defaultConfig = defaultConfig != null ? defaultConfig : new PromptConfig();
        this.annotationClass = annotationClass;
        this.excludeClasses = excludeClasses != null ? excludeClasses : new Class<?>[0];
        this.configId = StringUtils.hasText(configId) ? configId : UUID.randomUUID().toString();
    }

    /**
     * 验证配置的有效性
     *
     * @throws IllegalArgumentException 如果配置无效
     */
    public void validate() {
        if (basePackages == null || basePackages.length == 0) {
            throw new IllegalArgumentException("Base packages must be specified for PromptMapperScan");
        }

        if (StringUtils.hasText(chatModelBeanName) && chatModelClass != ChatModel.class) {
            throw new IllegalArgumentException("Cannot specify both chatModel and chatModelClass");
        }

        if (defaultConfig.getTemperature() != null) {
            double temp = defaultConfig.getTemperature();
            if (temp < 0 || temp > 1) {
                throw new IllegalArgumentException("Temperature must be between 0 and 1, but was: " + temp);
            }
        }

        if (defaultConfig.getMaxTokens() != null && defaultConfig.getMaxTokens() <= 0) {
            throw new IllegalArgumentException("MaxTokens must be positive, but was: " + defaultConfig.getMaxTokens());
        }

        if (defaultConfig.getTopP() != null) {
            double topP = defaultConfig.getTopP();
            if (topP < 0 || topP > 1) {
                throw new IllegalArgumentException("TopP must be between 0 and 1, but was: " + topP);
            }
        }
    }

    /**
     * 检查是否有有效的 ChatModel 配置
     *
     * @return 如果有有效的 ChatModel 配置则返回 true
     */
    public boolean hasValidChatModelConfig() {
        return StringUtils.hasText(chatModelBeanName) || chatModelClass != ChatModel.class;
    }

    // Getters and Setters

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptMapperScanConfiguration that = (PromptMapperScanConfiguration) o;
        return Objects.equals(configId, that.configId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(configId);
    }

    @Override
    public String toString() {
        return "PromptMapperScanConfiguration{" +
            "basePackages=" + Arrays.toString(basePackages) +
            ", chatModelBeanName='" + chatModelBeanName + '\'' +
            ", chatModelClass=" + chatModelClass +
            ", defaultConfig=" + defaultConfig +
            ", annotationClass=" + annotationClass +
            ", excludeClasses=" + Arrays.toString(excludeClasses) +
            ", configId='" + configId + '\'' +
            '}';
    }
}
