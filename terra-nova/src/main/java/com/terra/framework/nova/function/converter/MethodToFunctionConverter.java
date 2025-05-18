package com.terra.framework.nova.function.converter;

import com.terra.framework.nova.function.Function;
import com.terra.framework.nova.function.Parameter;
import com.terra.framework.nova.function.Schema;
import com.terra.framework.nova.function.annotation.AIFunction;
import com.terra.framework.nova.function.annotation.AIParameter;
import com.terra.framework.nova.function.handler.AnnotatedMethodFunctionHandler;
import com.terra.framework.nova.function.impl.SimpleFunction;
import com.terra.framework.nova.function.impl.SimpleParameter;
import com.terra.framework.nova.function.impl.SimpleSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.StringUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Converts annotated methods to Function objects.
 *
 * @author terra
 */
@Slf4j
public class MethodToFunctionConverter {

    /**
     * Convert a method to a Function object.
     *
     * @param method     the annotated method
     * @param annotation the AIFunction annotation
     * @param target     the target object containing the method
     * @return the Function object
     */
    public Function convert(Method method, AIFunction annotation, Object target) {
        String functionName = getFunctionName(method, annotation);
        List<Parameter> parameters = extractParameters(method);
        Schema responseSchema = createResponseSchema(method);

        return SimpleFunction.builder(functionName)
            .description(annotation.description())
            .addParameters(parameters)
            .responseSchema(responseSchema)
            .handler(new AnnotatedMethodFunctionHandler(target, method))
            .build();
    }

    private String getFunctionName(Method method, AIFunction annotation) {
        String name = annotation.name();
        return StringUtils.hasText(name) ? name : method.getName();
    }

    private List<Parameter> extractParameters(Method method) {
        List<Parameter> parameters = new ArrayList<>();
        java.lang.reflect.Parameter[] methodParams = method.getParameters();

        for (java.lang.reflect.Parameter param : methodParams) {
            AIParameter annotation = AnnotationUtils.findAnnotation(param, AIParameter.class);
            if (annotation == null) {
                log.warn("Parameter {} in method {} is not annotated with @AIParameter",
                    param.getName(), method.getName());
                continue;
            }

            parameters.add(createParameter(param, annotation));
        }

        return parameters;
    }

    private Parameter createParameter(java.lang.reflect.Parameter param, AIParameter annotation) {
        String name = StringUtils.hasText(annotation.name()) ? annotation.name() : param.getName();
        Schema schema = createParameterSchema(param, annotation);

        return new SimpleParameter(
            name,
            annotation.description(),
            schema,
            annotation.required()
        );
    }

    private Schema createParameterSchema(java.lang.reflect.Parameter param, AIParameter annotation) {
        String type = annotation.type();
        if (!StringUtils.hasText(type)) {
            type = inferSchemaType(param.getType());
        }
        return new SimpleSchema(type);
    }

    private Schema createResponseSchema(Method method) {
        return new SimpleSchema(inferSchemaType(method.getReturnType()));
    }

    private String inferSchemaType(Class<?> type) {
        if (type.equals(String.class)) {
            return "string";
        } else if (type.equals(Integer.class) || type.equals(int.class) ||
            type.equals(Long.class) || type.equals(long.class)) {
            return "integer";
        } else if (type.equals(Double.class) || type.equals(double.class) ||
            type.equals(Float.class) || type.equals(float.class)) {
            return "number";
        } else if (type.equals(Boolean.class) || type.equals(boolean.class)) {
            return "boolean";
        } else if (type.isArray() || List.class.isAssignableFrom(type)) {
            return "array";
        } else {
            return "object";
        }
    }
}
