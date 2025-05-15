package com.terra.framework.stream.annotation;

import com.terra.framework.stream.core.MessageProducer;
import com.terra.framework.stream.factory.MessageQueueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;

/**
 * 消息发布注解处理器
 * 处理@StreamPublish注解
 * 
 * @author terra
 */
@Slf4j
public class StreamPublishAnnotationBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private BeanFactory beanFactory;
    private final MessageQueueFactory messageQueueFactory;
    
    public StreamPublishAnnotationBeanPostProcessor(MessageQueueFactory messageQueueFactory) {
        this.messageQueueFactory = messageQueueFactory;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        Map<Method, StreamPublish> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<StreamPublish>) method ->
                        AnnotatedElementUtils.findMergedAnnotation(method, StreamPublish.class));
        
        if (annotatedMethods.isEmpty()) {
            return bean;
        }
        
        ProxyFactory proxyFactory = new ProxyFactory(bean);
        proxyFactory.setProxyTargetClass(true);
        
        proxyFactory.addAdvice((org.aopalliance.intercept.MethodInterceptor) invocation -> {
            Method method = invocation.getMethod();
            StreamPublish annotation = annotatedMethods.get(method);
            
            if (annotation != null) {
                Object result = invocation.proceed();
                publishResult(result, annotation);
                return result;
            }
            
            return invocation.proceed();
        });
        
        return proxyFactory.getProxy();
    }
    
    private void publishResult(Object result, StreamPublish annotation) {
        if (result == null) {
            log.warn("无法发布空结果");
            return;
        }
        
        String destination = annotation.destination();
        if (!StringUtils.hasText(destination)) {
            throw new IllegalArgumentException("@StreamPublish的destination属性不能为空");
        }
        
        String type = annotation.type();
        MessageProducer producer = messageQueueFactory.getMessageQueue(type).createProducer();
        
        producer.send(destination, result);
        log.debug("已发布消息到 {}, 类型: {}", destination, result.getClass().getSimpleName());
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
} 