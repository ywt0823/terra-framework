package com.terra.framework.stream.annotation;

import com.terra.framework.stream.core.MessageListener;
import com.terra.framework.stream.core.MessageQueue;
import com.terra.framework.stream.core.StreamMessage;
import com.terra.framework.stream.factory.MessageQueueFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.framework.AopProxyUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.MethodIntrospector;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 消息监听注解处理器
 * 处理@StreamListener注解
 * 
 * @author terra
 */
@Slf4j
public class StreamListenerAnnotationBeanPostProcessor implements BeanPostProcessor, BeanFactoryAware {

    private BeanFactory beanFactory;
    private final MessageQueueFactory messageQueueFactory;
    private final Map<String, Object> registeredListeners = new ConcurrentHashMap<>();
    
    public StreamListenerAnnotationBeanPostProcessor(MessageQueueFactory messageQueueFactory) {
        this.messageQueueFactory = messageQueueFactory;
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> targetClass = AopProxyUtils.ultimateTargetClass(bean);
        Map<Method, StreamListener> annotatedMethods = MethodIntrospector.selectMethods(targetClass,
                (MethodIntrospector.MetadataLookup<StreamListener>) method ->
                        AnnotatedElementUtils.findMergedAnnotation(method, StreamListener.class));
        
        if (annotatedMethods.isEmpty()) {
            return bean;
        }
        
        for (Map.Entry<Method, StreamListener> entry : annotatedMethods.entrySet()) {
            Method method = entry.getKey();
            StreamListener annotation = entry.getValue();
            
            processStreamListener(bean, method, annotation);
        }
        
        return bean;
    }
    
    private void processStreamListener(Object bean, Method method, StreamListener annotation) {
        String destination = annotation.destination();
        if (!StringUtils.hasText(destination)) {
            throw new IllegalArgumentException("@StreamListener的destination属性不能为空");
        }
        
        String type = annotation.type();
        MessageQueue messageQueue = messageQueueFactory.getMessageQueue(type);
        
        String group = annotation.group();
        if (!StringUtils.hasText(group)) {
            group = "group-" + UUID.randomUUID().toString();
        }
        
        // 创建消费者并订阅
        Object listener = createListener(bean, method);
        messageQueue.createConsumer(group).subscribe(destination, (MessageListener<Object>) listener);
        
        String listenerKey = bean.getClass().getName() + "#" + method.getName();
        registeredListeners.put(listenerKey, listener);
        
        log.info("已注册@StreamListener, 方法: {}, 目标队列: {}, 消费组: {}", 
                method, destination, group);
    }
    
    private Object createListener(Object bean, Method method) {
        return (MessageListener<Object>) message -> {
            try {
                // 检查参数类型
                Class<?>[] paramTypes = method.getParameterTypes();
                if (paramTypes.length == 0) {
                    method.invoke(bean);
                } else if (paramTypes.length == 1) {
                    if (StreamMessage.class.isAssignableFrom(paramTypes[0])) {
                        method.invoke(bean, message);
                    } else {
                        method.invoke(bean, message.getPayload());
                    }
                } else {
                    throw new IllegalArgumentException("@StreamListener方法参数数量必须为0或1");
                }
            } catch (Exception e) {
                log.error("调用@StreamListener方法时出错: {}", e.getMessage(), e);
                throw new RuntimeException("消息处理失败", e);
            }
        };
    }
    
    @Override
    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }
} 