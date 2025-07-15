package com.terra.framework.nova.prompt.factory;

import com.terra.framework.nova.prompt.config.PromptMapperScanConfiguration;
import com.terra.framework.nova.prompt.proxy.EnhancedPromptMapperProxy;
import com.terra.framework.nova.prompt.template.PromptTemplateRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.util.StringUtils;

import java.lang.reflect.Proxy;

/**
 * 增强的 PromptMapper 工厂 Bean。
 * <p>
 * 这个类继承自 {@link PromptMapperFactoryBean}，增加了对扫描配置的支持，
 * 允许为特定的 PromptMapper 接口绑定特定的 AI 模型和配置。
 * <p>
 * 相对于原始的 {@link PromptMapperFactoryBean}，这个增强版本支持：
 * <ul>
 *     <li>特定的 ChatModel 绑定</li>
 *     <li>独立的配置隔离</li>
 *     <li>更灵活的模型选择</li>
 * </ul>
 *
 * @param <T> PromptMapper 接口类型
 * @author DeavyJones
 * @since 1.0.0
 */
public class EnhancedPromptMapperFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware, BeanFactoryAware {

    private static final Logger logger = LoggerFactory.getLogger(EnhancedPromptMapperFactoryBean.class);

    /**
     * PromptMapper 接口类型
     */
    private final Class<T> mapperInterface;

    /**
     * Spring 应用上下文
     */
    private ApplicationContext applicationContext;

    /**
     * Bean 工厂
     */
    private BeanFactory beanFactory;

    /**
     * 扫描配置
     */
    private PromptMapperScanConfiguration scanConfiguration;

    /**
     * 构造函数
     *
     * @param mapperInterface PromptMapper 接口类型
     */
    public EnhancedPromptMapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    /**
     * 设置扫描配置
     *
     * @param scanConfiguration 扫描配置
     */
    public void setScanConfiguration(PromptMapperScanConfiguration scanConfiguration) {
        this.scanConfiguration = scanConfiguration;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    @Override
    public void setBeanFactory(BeanFactory beanFactory) {
        this.beanFactory = beanFactory;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() throws Exception {
        // 解析 ChatModel
        ChatModel chatModel = resolveChatModel();
        
        // 获取 PromptTemplateRegistry
        PromptTemplateRegistry registry = getPromptTemplateRegistry();
        
        // 创建增强的代理
        EnhancedPromptMapperProxy proxy = new EnhancedPromptMapperProxy(
            applicationContext,
            registry,
            mapperInterface,
            chatModel,
            scanConfiguration
        );
        
        // 创建代理实例
        T proxyInstance = (T) Proxy.newProxyInstance(
            mapperInterface.getClassLoader(),
            new Class[]{mapperInterface},
            proxy
        );
        
        logger.debug("Created PromptMapper proxy for interface: {} with configuration: {}", 
            mapperInterface.getName(), scanConfiguration.getConfigId());
        
        return proxyInstance;
    }

    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }

    /**
     * 解析 ChatModel
     *
     * @return ChatModel 实例
     * @throws Exception 如果解析失败
     */
    private ChatModel resolveChatModel() throws Exception {
        // 1. 优先使用指定的 Bean 名称
        if (StringUtils.hasText(scanConfiguration.getChatModelBeanName())) {
            try {
                ChatModel chatModel = beanFactory.getBean(scanConfiguration.getChatModelBeanName(), ChatModel.class);
                logger.debug("Resolved ChatModel by bean name: {}", scanConfiguration.getChatModelBeanName());
                return chatModel;
            } catch (NoSuchBeanDefinitionException e) {
                throw new IllegalStateException("ChatModel bean not found: " + scanConfiguration.getChatModelBeanName(), e);
            }
        }
        
        // 2. 使用指定的 Bean 类型
        if (scanConfiguration.getChatModelClass() != ChatModel.class) {
            try {
                ChatModel chatModel = beanFactory.getBean(scanConfiguration.getChatModelClass());
                logger.debug("Resolved ChatModel by class: {}", scanConfiguration.getChatModelClass().getName());
                return chatModel;
            } catch (NoSuchBeanDefinitionException e) {
                throw new IllegalStateException("ChatModel bean not found for class: " + scanConfiguration.getChatModelClass().getName(), e);
            }
        }
        
        // 3. 使用默认的 ChatModel
        try {
            ChatModel chatModel = beanFactory.getBean(ChatModel.class);
            logger.debug("Resolved default ChatModel");
            return chatModel;
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalStateException("No ChatModel bean found in application context", e);
        }
    }

    /**
     * 获取 PromptTemplateRegistry
     *
     * @return PromptTemplateRegistry 实例
     * @throws Exception 如果获取失败
     */
    private PromptTemplateRegistry getPromptTemplateRegistry() throws Exception {
        try {
            PromptTemplateRegistry registry = beanFactory.getBean(PromptTemplateRegistry.class);
            logger.debug("Retrieved PromptTemplateRegistry from application context");
            return registry;
        } catch (NoSuchBeanDefinitionException e) {
            throw new IllegalStateException("PromptTemplateRegistry bean not found in application context", e);
        }
    }

    /**
     * 获取 PromptMapper 接口类型
     *
     * @return 接口类型
     */
    public Class<T> getMapperInterface() {
        return mapperInterface;
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