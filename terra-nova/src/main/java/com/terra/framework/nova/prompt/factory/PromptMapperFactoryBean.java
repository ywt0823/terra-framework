package com.terra.framework.nova.prompt.factory;

import com.terra.framework.nova.prompt.proxy.PromptMapperProxy;
import com.terra.framework.nova.prompt.template.PromptTemplateRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import java.lang.reflect.Proxy;

/**
 * A Spring {@link FactoryBean} for creating prompt mapper proxy instances.
 * <p>
 * This factory is responsible for generating a dynamic proxy for a given
 * prompt mapper interface. The created proxy will be managed by Spring
 * as a bean.
 *
 * @author DeavyJones
 * @param <T> The type of the prompt mapper interface.
 */
public class PromptMapperFactoryBean<T> implements FactoryBean<T>, ApplicationContextAware {

    private final Class<T> mapperInterface;
    private ApplicationContext applicationContext;
    private PromptTemplateRegistry registry;
    private ChatModel defaultChatModel;

    public PromptMapperFactoryBean(Class<T> mapperInterface) {
        this.mapperInterface = mapperInterface;
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public void setRegistry(PromptTemplateRegistry registry) {
        this.registry = registry;
    }

    public void setDefaultChatModel(ChatModel defaultChatModel) {
        this.defaultChatModel = defaultChatModel;
    }

    @Override
    @SuppressWarnings("unchecked")
    public T getObject() {
        PromptMapperProxy proxy = new PromptMapperProxy(
                applicationContext, 
                registry, 
                mapperInterface, 
                defaultChatModel
        );
        return (T) Proxy.newProxyInstance(
                mapperInterface.getClassLoader(),
                new Class[]{mapperInterface},
                proxy
        );
    }

    @Override
    public Class<?> getObjectType() {
        return this.mapperInterface;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
} 