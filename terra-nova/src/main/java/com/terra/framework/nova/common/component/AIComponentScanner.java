package com.terra.framework.nova.common.component;

import com.terra.framework.nova.common.annotation.AIComponent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;

/**
 * AI组件扫描器，负责扫描并注册组件
 *
 * @author terra-nova
 */
@Slf4j
public class AIComponentScanner implements BeanPostProcessor {
    
    private final AIComponentManager componentManager;
    private final AIComponentAdapter componentAdapter;
    private final String[] basePackages;
    private final Map<String, Object> registeredComponents = new HashMap<>();
    
    /**
     * 构造函数
     *
     * @param componentManager 组件管理器
     * @param componentAdapter 组件适配器
     * @param basePackages     基础包路径
     */
    public AIComponentScanner(
        AIComponentManager componentManager,
        AIComponentAdapter componentAdapter,
        String[] basePackages) {
        this.componentManager = componentManager;
        this.componentAdapter = componentAdapter;
        this.basePackages = basePackages != null && basePackages.length > 0 ? basePackages : new String[]{"com.terra"};
        log.info("Initialized AIComponentScanner with base packages: {}", String.join(", ", this.basePackages));
    }
    
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        if (shouldScanClass(bean.getClass())) {
            scanForAnnotatedMethods(bean, beanName);
        }
        return bean;
    }
    
    private void scanForAnnotatedMethods(Object bean, String beanName) {
        Class<?> targetClass = bean.getClass();
        
        // 获取所有方法（包括继承的公共方法）
        ReflectionUtils.doWithMethods(targetClass, method -> {
            AIComponent annotation = AnnotationUtils.findAnnotation(method, AIComponent.class);
            if (annotation != null) {
                processAnnotatedMethod(method, bean, beanName);
            }
        }, method -> !method.isSynthetic() && !method.isBridge() && Modifier.isPublic(method.getModifiers()));
    }
    
    private void processAnnotatedMethod(Method method, Object bean, String beanName) {
        try {
            String methodKey = bean.getClass().getName() + "." + method.getName();
            
            // 避免重复注册
            if (registeredComponents.containsKey(methodKey)) {
                return;
            }
            
            // 转换并注册组件
            AIComponentInfo componentInfo = componentAdapter.convertMethod(method, bean);
            componentManager.registerComponent(componentInfo);
            registeredComponents.put(methodKey, bean);
            
            log.info("Registered @AIComponent method as component: {}.{} -> {}",
                bean.getClass().getSimpleName(), method.getName(), componentInfo.getName());
        } catch (Exception e) {
            log.error("Failed to register @AIComponent method: {}.{}",
                bean.getClass().getSimpleName(), method.getName(), e);
        }
    }
    
    private boolean shouldScanClass(Class<?> clazz) {
        if (basePackages == null || basePackages.length == 0) {
            return true;
        }
        
        String className = clazz.getName();
        for (String basePackage : basePackages) {
            if (className.startsWith(basePackage)) {
                return true;
            }
        }
        return false;
    }
} 