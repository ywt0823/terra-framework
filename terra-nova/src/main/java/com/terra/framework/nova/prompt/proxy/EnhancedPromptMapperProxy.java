package com.terra.framework.nova.prompt.proxy;

import com.terra.framework.nova.prompt.config.PromptConfig;
import com.terra.framework.nova.prompt.config.PromptMapperScanConfiguration;
import com.terra.framework.nova.prompt.template.PromptTemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.context.ApplicationContext;

import java.lang.reflect.Method;

/**
 * 增强的 PromptMapper 代理类。
 * <p>
 * 这个类继承自 {@link PromptMapperProxy}，增加了对扫描配置的支持，
 * 允许为特定的 PromptMapper 接口使用特定的配置。
 * <p>
 * 相对于原始的 {@link PromptMapperProxy}，这个增强版本支持：
 * <ul>
 *     <li>扫描配置的默认值应用</li>
 *     <li>配置优先级管理</li>
 *     <li>更灵活的配置合并</li>
 * </ul>
 *
 * @author DeavyJones
 * @since 1.0.0
 */
public class EnhancedPromptMapperProxy extends PromptMapperProxy {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedPromptMapperProxy.class);

    /**
     * 扫描配置
     */
    private final PromptMapperScanConfiguration scanConfiguration;

    /**
     * 构造函数
     *
     * @param applicationContext Spring 应用上下文
     * @param registry          模板注册中心
     * @param mapperInterface   PromptMapper 接口类型
     * @param chatModel         ChatModel 实例
     * @param scanConfiguration 扫描配置
     */
    public EnhancedPromptMapperProxy(ApplicationContext applicationContext,
                                   PromptTemplateRegistry registry,
                                   Class<?> mapperInterface,
                                   ChatModel chatModel,
                                   PromptMapperScanConfiguration scanConfiguration) {
        super(applicationContext, registry, mapperInterface, chatModel);
        this.scanConfiguration = scanConfiguration;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        // 在方法调用前记录配置信息
        logger.debug("Invoking method: {} with scan configuration: {}", 
            method.getName(), scanConfiguration.getConfigId());
        
        // 调用父类的方法
        return super.invoke(proxy, method, args);
    }

    /**
     * 解析有效的配置。
     * <p>
     * 配置优先级（从高到低）：
     * 1. XML 模板中的配置
     * 2. 扫描配置中的默认配置
     * 3. 全局默认配置
     *
     * @param templateConfig 模板配置
     * @return 有效的配置
     */
    protected PromptConfig resolveEffectiveConfig(PromptConfig templateConfig) {
        if (templateConfig == null) {
            templateConfig = new PromptConfig();
        }

        // 合并扫描配置中的默认配置
        PromptConfig effectiveConfig = templateConfig;
        if (scanConfiguration != null && scanConfiguration.getDefaultConfig() != null) {
            effectiveConfig = templateConfig.mergeWith(scanConfiguration.getDefaultConfig());
            logger.debug("Merged template config with scan default config for method");
        }

        return effectiveConfig;
    }

    /**
     * 解析适当的 ChatModel。
     * <p>
     * 由于 ChatModel 已经在 FactoryBean 中解析，这里直接返回传入的 ChatModel。
     *
     * @param templateConfig 模板配置
     * @return ChatModel 实例
     */
    protected ChatModel resolveChatModel(PromptConfig templateConfig) {
        // 使用父类的默认 ChatModel（已经在 FactoryBean 中解析）
        ChatModel chatModel = getDefaultChatModel();
        
        logger.debug("Using ChatModel from scan configuration: {}", 
            chatModel.getClass().getSimpleName());
        
        return chatModel;
    }

    /**
     * 获取默认的 ChatModel
     *
     * @return ChatModel 实例
     */
    private ChatModel getDefaultChatModel() {
        // 这里通过反射获取父类的 defaultChatModel 字段
        // 由于访问限制，我们需要通过其他方式获取
        try {
            java.lang.reflect.Field field = PromptMapperProxy.class.getDeclaredField("defaultChatModel");
            field.setAccessible(true);
            return (ChatModel) field.get(this);
        } catch (Exception e) {
            logger.warn("Failed to get default ChatModel from parent class", e);
            // 如果获取失败，返回 null，让父类处理
            return null;
        }
    }

    /**
     * 获取扫描配置
     *
     * @return 扫描配置
     */
    public PromptMapperScanConfiguration getScanConfiguration() {
        return scanConfiguration;
    }
} 