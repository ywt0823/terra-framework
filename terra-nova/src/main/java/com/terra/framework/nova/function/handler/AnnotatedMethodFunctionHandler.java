package com.terra.framework.nova.function.handler;

import com.terra.framework.nova.function.FunctionHandler;
import com.terra.framework.nova.common.annotation.AIParameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.ReflectionUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * Function handler that executes annotated methods.
 *
 * @author terra
 */
@Slf4j
public class AnnotatedMethodFunctionHandler implements FunctionHandler {

    private final Object target;
    private final Method method;
    private final Map<String, Integer> parameterIndexMap;

    public AnnotatedMethodFunctionHandler(Object target, Method method) {
        this.target = target;
        this.method = method;
        this.parameterIndexMap = buildParameterIndexMap(method);
        ReflectionUtils.makeAccessible(method);
    }

    @Override
    public Object handle(Map<String, Object> parameters) {
        try {
            Object[] args = prepareArguments(parameters);
            return method.invoke(target, args);
        } catch (Exception e) {
            log.error("Failed to execute function: {}", method.getName(), e);
            throw new RuntimeException("Function execution failed", e);
        }
    }

    private Map<String, Integer> buildParameterIndexMap(Method method) {
        Map<String, Integer> indexMap = new HashMap<>();
        Parameter[] parameters = method.getParameters();

        for (int i = 0; i < parameters.length; i++) {
            Parameter param = parameters[i];
            AIParameter annotation = AnnotationUtils.findAnnotation(param, AIParameter.class);
            
            String paramName;
            if (annotation != null && StringUtils.hasText(annotation.name())) {
                paramName = annotation.name();
            } else {
                paramName = param.getName();
            }
            
            indexMap.put(paramName, i);
        }

        return indexMap;
    }

    private Object[] prepareArguments(Map<String, Object> parameters) {
        Object[] args = new Object[method.getParameterCount()];
        Parameter[] methodParams = method.getParameters();

        for (Map.Entry<String, Integer> entry : parameterIndexMap.entrySet()) {
            String paramName = entry.getKey();
            int index = entry.getValue();
            
            Object value = parameters.get(paramName);
            if (value != null) {
                args[index] = convertValue(value, methodParams[index].getType());
            }
        }

        return args;
    }

    private Object convertValue(Object value, Class<?> targetType) {
        // Basic type conversion logic
        if (value == null || targetType.isInstance(value)) {
            return value;
        }

        // Handle primitive types
        if (targetType.isPrimitive()) {
            if (targetType == int.class) {
                return ((Number) value).intValue();
            } else if (targetType == long.class) {
                return ((Number) value).longValue();
            } else if (targetType == double.class) {
                return ((Number) value).doubleValue();
            } else if (targetType == float.class) {
                return ((Number) value).floatValue();
            } else if (targetType == boolean.class) {
                return Boolean.valueOf(value.toString());
            }
        }

        // Handle String conversion
        if (targetType == String.class) {
            return value.toString();
        }

        // For more complex types, you might want to use a proper conversion service
        throw new IllegalArgumentException(
            String.format("Unsupported type conversion from %s to %s", 
                value.getClass().getName(), targetType.getName()));
    }
} 