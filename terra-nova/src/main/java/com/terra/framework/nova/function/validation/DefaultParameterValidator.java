package com.terra.framework.nova.function.validation;

import com.terra.framework.nova.function.Parameter;
import com.terra.framework.nova.function.Schema;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 默认参数验证器实现
 *
 * @author terra-nova
 */
public class DefaultParameterValidator implements ParameterValidator {
    
    @Override
    public ValidationResult validate(Map<String, Object> parameters, List<Parameter> parameterDefinitions) {
        List<ValidationResult.ValidationError> errors = new ArrayList<>();
        
        // 验证必需参数
        for (Parameter param : parameterDefinitions) {
            String name = param.getName();
            if (param.isRequired() && !parameters.containsKey(name)) {
                errors.add(new ValidationResult.ValidationError(name, "Required parameter is missing"));
                continue;
            }
            
            if (parameters.containsKey(name)) {
                Object value = parameters.get(name);
                // 验证类型
                if (!validateType(value, param.getSchema())) {
                    errors.add(new ValidationResult.ValidationError(name, 
                            "Parameter type mismatch, expected: " + param.getSchema().getType()));
                }
            }
        }
        
        if (errors.isEmpty()) {
            return ValidationResult.valid();
        }
        return ValidationResult.invalid(errors);
    }
    
    /**
     * 验证值类型是否符合模式定义
     *
     * @param value 参数值
     * @param schema 模式
     * @return 是否符合类型要求
     */
    private boolean validateType(Object value, Schema schema) {
        if (value == null) {
            return true; // 允许空值，由required约束控制是否必填
        }
        
        String schemaType = schema.getType().toLowerCase();
        switch (schemaType) {
            case "string":
                return value instanceof String;
            case "number":
                return value instanceof Number;
            case "integer":
                return value instanceof Integer || value instanceof Long;
            case "boolean":
                return value instanceof Boolean;
            case "array":
                return value instanceof List || value.getClass().isArray();
            case "object":
                return value instanceof Map;
            default:
                return true; // 未知类型默认通过
        }
    }
} 