package com.terra.framework.nova.function.scanner;

import com.terra.framework.nova.function.Function;
import com.terra.framework.nova.function.FunctionRegistry;
import com.terra.framework.nova.function.annotation.AIFunction;
import com.terra.framework.nova.function.converter.MethodToFunctionConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Scanner that processes beans to find and register AI functions.
 *
 * @author terra
 */
@Slf4j
public class FunctionScanner implements BeanPostProcessor {

    private final FunctionRegistry functionRegistry;
    private final MethodToFunctionConverter converter;

    public FunctionScanner(FunctionRegistry functionRegistry, MethodToFunctionConverter converter) {
        this.functionRegistry = functionRegistry;
        this.converter = converter;
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        scanAndRegisterFunctions(bean, beanName);
        return bean;
    }

    private void scanAndRegisterFunctions(Object bean, String beanName) {
        Class<?> targetClass = bean.getClass();
        List<Method> candidateMethods = new ArrayList<>();

        ReflectionUtils.doWithMethods(targetClass, candidateMethods::add, 
            method -> AnnotationUtils.findAnnotation(method, AIFunction.class) != null);

        for (Method method : candidateMethods) {
            registerFunction(method, bean, beanName);
        }
    }

    private void registerFunction(Method method, Object bean, String beanName) {
        try {
            AIFunction annotation = AnnotationUtils.findAnnotation(method, AIFunction.class);
            if (annotation == null) {
                return;
            }

            Function function = converter.convert(method, annotation, bean);
            String functionName = function.getName();

            log.info("Registering AI function: {} from bean: {}", functionName, beanName);
            functionRegistry.registerFunction(function);
        } catch (Exception e) {
            log.error("Failed to register function for method: {} in bean: {}", method.getName(), beanName, e);
        }
    }
} 