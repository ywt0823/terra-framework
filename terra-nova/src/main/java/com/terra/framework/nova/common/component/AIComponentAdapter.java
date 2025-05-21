package com.terra.framework.nova.common.component;

import com.terra.framework.nova.agent.tool.Tool;
import com.terra.framework.nova.agent.tool.ToolParameter;
import com.terra.framework.nova.agent.tool.handler.MethodToolHandler;
import com.terra.framework.nova.common.annotation.AIComponent;
import com.terra.framework.nova.common.annotation.AIParameter;
import com.terra.framework.nova.common.annotation.ComponentType;
import com.terra.framework.nova.function.Function;
import com.terra.framework.nova.function.Parameter;
import com.terra.framework.nova.function.Schema;
import com.terra.framework.nova.function.handler.AnnotatedMethodFunctionHandler;
import com.terra.framework.nova.function.impl.SimpleFunction;
import com.terra.framework.nova.function.impl.SimpleParameter;
import com.terra.framework.nova.function.impl.SimpleSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.AnnotationUtils;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * AI组件适配器，负责将方法转换为Tool和Function
 *
 * @author terra-nova
 */
@Slf4j
public class AIComponentAdapter {
    
    /**
     * 转换方法为组件信息
     *
     * @param method 方法
     * @param bean   方法所属对象
     * @return 组件信息
     */
    public AIComponentInfo convertMethod(Method method, Object bean) {
        AIComponent annotation = AnnotationUtils.findAnnotation(method, AIComponent.class);
        if (annotation == null) {
            throw new IllegalArgumentException("方法未标记@AIComponent注解: " + method.getName());
        }
        
        String name = getComponentName(method, annotation);
        String description = annotation.description();
        ComponentType[] types = annotation.types();
        String category = annotation.category();
        
        // 构建组件信息
        AIComponentInfo.AIComponentInfoBuilder builder = AIComponentInfo.builder()
            .name(name)
            .description(description)
            .types(types)
            .category(category)
            .targetBean(bean)
            .targetMethod(method);
        
        // 根据类型创建相应的Tool或Function
        for (ComponentType type : types) {
            if (type == ComponentType.TOOL) {
                Tool tool = convertToTool(method, bean, name, description);
                builder.tool(tool);
            } else if (type == ComponentType.FUNCTION) {
                Function function = convertToFunction(method, bean, name, description, category);
                builder.function(function);
            }
        }
        
        return builder.build();
    }
    
    /**
     * 将方法转换为Tool
     *
     * @param method      方法
     * @param bean        方法所属对象
     * @param name        名称
     * @param description 描述
     * @return Tool实现
     */
    private Tool convertToTool(Method method, Object bean, String name, String description) {
        List<ToolParameter> parameters = extractToolParameters(method);
        return new MethodToolHandler(name, description, parameters, bean, method);
    }
    
    /**
     * 将方法转换为Function
     *
     * @param method      方法
     * @param bean        方法所属对象
     * @param name        名称
     * @param description 描述
     * @param category    分类
     * @return Function实现
     */
    private Function convertToFunction(Method method, Object bean, String name, String description, String category) {
        List<Parameter> parameters = extractFunctionParameters(method);
        Schema responseSchema = createResponseSchema(method);
        
        return SimpleFunction.builder(name)
            .description(description)
            .addParameters(parameters)
            .responseSchema(responseSchema)
            .handler(new AnnotatedMethodFunctionHandler(bean, method))
            .build();
    }
    
    /**
     * 获取组件名称
     *
     * @param method     方法
     * @param annotation 注解
     * @return 组件名称
     */
    private String getComponentName(Method method, AIComponent annotation) {
        return annotation.name().isEmpty() ? method.getName() : annotation.name();
    }
    
    /**
     * 提取Tool参数
     *
     * @param method 方法
     * @return 参数列表
     */
    private List<ToolParameter> extractToolParameters(Method method) {
        List<ToolParameter> parameters = new ArrayList<>();
        java.lang.reflect.Parameter[] methodParams = method.getParameters();
        
        for (java.lang.reflect.Parameter param : methodParams) {
            AIParameter annotation = param.getAnnotation(AIParameter.class);
            if (annotation != null) {
                parameters.add(createToolParameter(param, annotation));
            } else {
                // 创建默认参数
                parameters.add(new ToolParameter(
                    param.getName(),
                    inferParamType(param.getType()),
                    "参数 " + param.getName(),
                    true
                ));
            }
        }
        
        return parameters;
    }
    
    /**
     * 提取Function参数
     *
     * @param method 方法
     * @return 参数列表
     */
    private List<Parameter> extractFunctionParameters(Method method) {
        List<Parameter> parameters = new ArrayList<>();
        java.lang.reflect.Parameter[] methodParams = method.getParameters();
        
        for (java.lang.reflect.Parameter param : methodParams) {
            AIParameter annotation = param.getAnnotation(AIParameter.class);
            if (annotation != null) {
                parameters.add(createFunctionParameter(param, annotation));
            } else {
                // 创建默认参数
                parameters.add(new SimpleParameter(
                    param.getName(),
                    "参数 " + param.getName(),
                    SimpleSchema.createStringSchema(),
                    true
                ));
            }
        }
        
        return parameters;
    }
    
    /**
     * 创建Tool参数
     *
     * @param param      方法参数
     * @param annotation 注解
     * @return Tool参数
     */
    private ToolParameter createToolParameter(java.lang.reflect.Parameter param, AIParameter annotation) {
        String name = annotation.name().isEmpty() ? param.getName() : annotation.name();
        String description = annotation.description();
        String type = annotation.type().isEmpty() ? inferParamType(param.getType()) : annotation.type();
        boolean required = annotation.required();
        Object defaultValue = annotation.defaultValue().isEmpty() ? null : annotation.defaultValue();
        
        return ToolParameter.builder()
            .name(name)
            .type(type)
            .description(description)
            .required(required)
            .defaultValue(defaultValue)
            .build();
    }
    
    /**
     * 创建Function参数
     *
     * @param param      方法参数
     * @param annotation 注解
     * @return Function参数
     */
    private Parameter createFunctionParameter(java.lang.reflect.Parameter param, AIParameter annotation) {
        String name = annotation.name().isEmpty() ? param.getName() : annotation.name();
        String description = annotation.description();
        boolean required = annotation.required();
        
        // 创建参数Schema
        Schema schema = createParameterSchema(param, annotation);
        
        return new SimpleParameter(name, description, schema, required);
    }
    
    /**
     * 创建参数Schema
     *
     * @param param      方法参数
     * @param annotation 注解
     * @return Schema
     */
    private Schema createParameterSchema(java.lang.reflect.Parameter param, AIParameter annotation) {
        String type = annotation.type().isEmpty() ? inferSchemaType(param.getType()) : annotation.type();
        
        switch (type) {
            case "string":
                return SimpleSchema.createStringSchema();
            case "number":
                return SimpleSchema.createNumberSchema();
            case "boolean":
                return SimpleSchema.createBooleanSchema();
            case "array":
                return SimpleSchema.createArraySchema(SimpleSchema.createStringSchema());
            default:
                return SimpleSchema.createStringSchema();
        }
    }
    
    /**
     * 创建返回值Schema
     *
     * @param method 方法
     * @return Schema
     */
    private Schema createResponseSchema(Method method) {
        Class<?> returnType = method.getReturnType();
        String type = inferSchemaType(returnType);
        
        switch (type) {
            case "string":
                return SimpleSchema.createStringSchema();
            case "number":
                return SimpleSchema.createNumberSchema();
            case "boolean":
                return SimpleSchema.createBooleanSchema();
            case "array":
                return SimpleSchema.createArraySchema(SimpleSchema.createStringSchema());
            default:
                return SimpleSchema.createStringSchema();
        }
    }
    
    /**
     * 推断参数类型
     *
     * @param type Java类型
     * @return 参数类型字符串
     */
    private String inferParamType(Class<?> type) {
        if (type == String.class) {
            return "string";
        } else if (type == Integer.class || type == int.class || 
                   type == Long.class || type == long.class) {
            return "number";
        } else if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        } else if (type == Double.class || type == double.class ||
                   type == Float.class || type == float.class) {
            return "number";
        } else if (type.isArray() || List.class.isAssignableFrom(type)) {
            return "array";
        } else {
            return "object";
        }
    }
    
    /**
     * 推断Schema类型
     *
     * @param type Java类型
     * @return Schema类型
     */
    private String inferSchemaType(Class<?> type) {
        if (type == String.class) {
            return "string";
        } else if (type == Integer.class || type == int.class || 
                   type == Long.class || type == long.class ||
                   type == Double.class || type == double.class ||
                   type == Float.class || type == float.class) {
            return "number";
        } else if (type == Boolean.class || type == boolean.class) {
            return "boolean";
        } else if (type.isArray() || List.class.isAssignableFrom(type)) {
            return "array";
        } else if (type == void.class || type == Void.class) {
            return "null";
        } else {
            return "object";
        }
    }
} 