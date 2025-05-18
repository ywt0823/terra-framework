package com.terra.framework.nova.function.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 参数验证结果
 *
 * @author terra-nova
 */
public class ValidationResult {
    
    private final boolean valid;
    private final List<ValidationError> errors;
    
    private ValidationResult(boolean valid, List<ValidationError> errors) {
        this.valid = valid;
        this.errors = errors != null ? Collections.unmodifiableList(errors) : Collections.emptyList();
    }
    
    /**
     * 是否验证通过
     *
     * @return 是否通过
     */
    public boolean isValid() {
        return valid;
    }
    
    /**
     * 获取验证错误列表
     *
     * @return 错误列表
     */
    public List<ValidationError> getErrors() {
        return errors;
    }
    
    /**
     * 创建验证通过的结果
     *
     * @return 验证结果
     */
    public static ValidationResult valid() {
        return new ValidationResult(true, null);
    }
    
    /**
     * 创建验证失败的结果
     *
     * @param errors 错误列表
     * @return 验证结果
     */
    public static ValidationResult invalid(List<ValidationError> errors) {
        return new ValidationResult(false, errors);
    }
    
    /**
     * 创建验证失败的结果
     *
     * @param parameterName 参数名称
     * @param errorMessage 错误信息
     * @return 验证结果
     */
    public static ValidationResult invalid(String parameterName, String errorMessage) {
        List<ValidationError> errors = new ArrayList<>();
        errors.add(new ValidationError(parameterName, errorMessage));
        return invalid(errors);
    }
    
    /**
     * 验证错误信息
     */
    public static class ValidationError {
        private final String parameterName;
        private final String message;
        
        public ValidationError(String parameterName, String message) {
            this.parameterName = parameterName;
            this.message = message;
        }
        
        public String getParameterName() {
            return parameterName;
        }
        
        public String getMessage() {
            return message;
        }
        
        @Override
        public String toString() {
            return String.format("%s: %s", parameterName, message);
        }
    }
} 