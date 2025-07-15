package com.terra.framework.nova.prompt.annotation;

import java.lang.annotation.*;

/**
 * 容器注解，用于支持多个 {@link PromptMapperScan} 注解。
 * <p>
 * 这个注解是 {@link PromptMapperScan} 的容器，当在同一个类上使用多个 
 * {@code @PromptMapperScan} 注解时，Java 会自动将其包装在这个容器注解中。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Configuration
 * @PromptMapperScan(basePackages = "com.example.prompts.creative", chatModel = "openAiChatModel")
 * @PromptMapperScan(basePackages = "com.example.prompts.analytical", chatModel = "deepSeekChatModel")
 * public class PromptConfiguration {
 * }
 * }</pre>
 *
 * @author DeavyJones
 * @since 1.0.0
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Documented
public @interface PromptMapperScans {

    /**
     * 包含的 {@link PromptMapperScan} 注解数组。
     * 
     * @return PromptMapperScan 注解数组
     */
    PromptMapperScan[] value();
} 