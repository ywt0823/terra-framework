package com.terra.framework.nova.function.validation;

import com.terra.framework.nova.function.Parameter;

import java.util.List;
import java.util.Map;

/**
 * 函数参数验证器接口
 *
 * @author terra-nova
 */
public interface ParameterValidator {
    
    /**
     * 验证参数
     *
     * @param parameters 参数Map
     * @param parameterDefinitions 参数定义列表
     * @return 验证结果
     */
    ValidationResult validate(Map<String, Object> parameters, List<Parameter> parameterDefinitions);
} 