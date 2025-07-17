package com.terra.framework.nova.prompt.annotation;

import com.terra.framework.nova.prompt.registrar.PromptMapperScannerRegistrar;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.annotation.Import;
import org.springframework.core.annotation.AliasFor;

import java.lang.annotation.*;

/**
 * 扫描指定包路径下的 {@link PromptMapper} 接口，并为其配置特定的 AI 模型和参数。
 * <p>
 * 这个注解类似于 MyBatis 的 {@code @MapperScan}，允许开发者为不同的 PromptMapper
 * 指定不同的 AI 模型配置，实现模型隔离和专门化配置。
 * <p>
 * 使用示例：
 * <pre>{@code
 * @Configuration
 * @PromptMapperScan(
 *     basePackages = "com.example.prompts.creative",
 *     chatModel = "openAiChatModel",
 *     temperature = 0.9,
 *     modelName = "gpt-4"
 * )
 * @PromptMapperScan(
 *     basePackages = "com.example.prompts.analytical",
 *     chatModel = "deepSeekChatModel",
 *     temperature = 0.3,
 *     modelName = "deepseek-chat"
 * )
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
@Import(PromptMapperScannerRegistrar.class)
@Repeatable(PromptMapperScans.class)
public @interface PromptMapperScan {

    /**
     * 扫描的基础包路径。
     * <p>
     * 这是 {@link #basePackages} 的别名，用于简化单个包的配置。
     *
     * @return 基础包路径数组
     */
    @AliasFor("basePackages")
    String[] value() default {};

    /**
     * 扫描的基础包路径。
     * <p>
     * 指定要扫描 {@link PromptMapper} 接口的包路径。
     *
     * @return 基础包路径数组
     */
    @AliasFor("value")
    String[] basePackages() default {};

    /**
     * 扫描的基础包类。
     * <p>
     * 指定基础包类，扫描器将扫描这些类所在的包。
     *
     * @return 基础包类数组
     */
    Class<?>[] basePackageClasses() default {};

    /**
     * 指定使用的 ChatModel Bean 名称。
     * <p>
     * 当指定了 Bean 名称时，扫描到的所有 PromptMapper 将使用这个特定的 ChatModel 实例。
     * 这个属性与 {@link #chatModelClass} 互斥，同时指定时会抛出异常。
     *
     * @return ChatModel Bean 名称
     */
    String chatModel() default "";

    /**
     * 指定使用的 ChatModel Bean 类型。
     * <p>
     * 当指定了 Bean 类型时，扫描到的所有 PromptMapper 将使用这个类型的 ChatModel 实例。
     * 这个属性与 {@link #chatModel} 互斥，同时指定时会抛出异常。
     *
     * @return ChatModel Bean 类型
     */
    Class<? extends ChatModel> chatModelClass() default ChatModel.class;

    /**
     * 指定使用的模型名称。
     * <p>
     * 用于设置 ChatModel 的具体模型（如 "gpt-4", "deepseek-chat" 等）。
     *
     * @return 模型名称
     */
    String modelName() default "";

    /**
     * 设置默认的温度值。
     * <p>
     * 温度值控制生成文本的随机性，范围通常在 0.0 到 1.0 之间。
     * 值为 -1 表示使用系统默认值。
     *
     * @return 温度值
     */
    double temperature() default -1;

    /**
     * 设置默认的最大 token 数。
     * <p>
     * 控制生成文本的最大长度。
     * 值为 -1 表示使用系统默认值。
     *
     * @return 最大 token 数
     */
    int maxTokens() default -1;

    /**
     * 设置默认的 top-p 值。
     * <p>
     * 用于核采样，控制生成文本的多样性。
     * 值为 -1 表示使用系统默认值。
     *
     * @return top-p 值
     */
    double topP() default -1;

    /**
     * 指定要扫描的注解类型。
     * <p>
     * 默认扫描 {@link Annotation} 注解，可以通过此属性扩展到其他注解类型。
     *
     * @return 注解类型
     */
    Class<? extends Annotation> annotationClass() default Annotation.class;

    /**
     * 排除的类型。
     * <p>
     * 指定要排除的类，即使这些类标注了 {@link #annotationClass} 指定的注解，
     * 也不会被扫描注册。
     *
     * @return 排除的类型数组
     */
    Class<?>[] excludeClasses() default {};

    /**
     * 扫描配置的唯一标识符。
     * <p>
     * 用于区分不同的扫描配置，在监控和调试时很有用。
     * 如果未指定，将自动生成一个唯一标识符。
     *
     * @return 配置标识符
     */
    String configId() default "";
}
