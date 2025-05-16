package com.terra.framework.nova.tool;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 抽象工具基类，实现Tool接口的基本功能
 *
 * @author terra-nova
 */
@Slf4j
public abstract class AbstractTool implements Tool {
    
    /**
     * 工具名称
     */
    @Getter
    protected final String name;
    
    /**
     * 工具描述
     */
    @Getter
    protected final String description;
    
    /**
     * 工具类别
     */
    @Getter
    protected final String category;
    
    /**
     * 参数描述
     */
    protected final Map<String, ParameterDescription> parameterDescriptions;
    
    /**
     * 是否需要认证
     */
    @Getter
    protected final boolean requiresAuthentication;
    
    /**
     * 是否异步执行
     */
    @Getter
    protected final boolean isAsync;
    
    /**
     * 构造函数
     *
     * @param name 工具名称
     * @param description 工具描述
     */
    protected AbstractTool(String name, String description) {
        this(name, description, "general", false, false);
    }
    
    /**
     * 构造函数
     *
     * @param name 工具名称
     * @param description 工具描述
     * @param category 工具类别
     * @param requiresAuthentication 是否需要认证
     * @param isAsync 是否异步执行
     */
    protected AbstractTool(String name, String description, String category, 
                        boolean requiresAuthentication, boolean isAsync) {
        this.name = name;
        this.description = description;
        this.category = category;
        this.requiresAuthentication = requiresAuthentication;
        this.isAsync = isAsync;
        this.parameterDescriptions = initializeParameterDescriptions();
    }
    
    /**
     * 初始化参数描述
     *
     * @return 参数描述映射
     */
    protected abstract Map<String, ParameterDescription> initializeParameterDescriptions();
    
    @Override
    public Map<String, ParameterDescription> getParameterDescriptions() {
        return Collections.unmodifiableMap(parameterDescriptions);
    }
    
    /**
     * 验证参数
     *
     * @param parameters 参数映射
     * @throws ToolExecutionException 参数验证失败时抛出异常
     */
    protected void validateParameters(Map<String, String> parameters) throws ToolExecutionException {
        // 检查所有必需参数是否存在
        StringBuilder missingParams = new StringBuilder();
        
        for (Map.Entry<String, ParameterDescription> entry : parameterDescriptions.entrySet()) {
            String paramName = entry.getKey();
            ParameterDescription paramDesc = entry.getValue();
            
            if (paramDesc.isRequired() && 
                (parameters == null || !parameters.containsKey(paramName) || parameters.get(paramName) == null)) {
                
                if (missingParams.length() > 0) {
                    missingParams.append(", ");
                }
                
                missingParams.append(paramName);
            }
        }
        
        if (missingParams.length() > 0) {
            throw new ToolExecutionException(
                    getName(),
                    "MISSING_PARAMS",
                    "缺少必需的参数: " + missingParams
            );
        }
    }
    
    /**
     * 获取参数值，如果不存在则使用默认值
     *
     * @param parameters 参数映射
     * @param paramName 参数名称
     * @return 参数值
     */
    protected String getParameterOrDefault(Map<String, String> parameters, String paramName) {
        if (parameters == null || !parameters.containsKey(paramName) || parameters.get(paramName) == null) {
            ParameterDescription paramDesc = parameterDescriptions.get(paramName);
            if (paramDesc != null) {
                return paramDesc.getDefaultValue();
            }
            return null;
        }
        return parameters.get(paramName);
    }
    
    /**
     * 执行工具前
     *
     * @param parameters 参数映射
     * @throws ToolExecutionException 执行异常
     */
    protected void beforeExecute(Map<String, String> parameters) throws ToolExecutionException {
        validateParameters(parameters);
    }
    
    /**
     * 执行工具后
     *
     * @param parameters 参数映射
     * @param result 执行结果
     * @return 处理后的结果
     */
    protected String afterExecute(Map<String, String> parameters, String result) {
        return result;
    }
    
    @Override
    public final String execute(Map<String, String> parameters) throws ToolExecutionException {
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        
        try {
            log.debug("执行工具 '{}' 开始, 参数: {}", name, parameters);
            
            // 前置处理
            beforeExecute(parameters);
            
            // 执行核心逻辑
            String result = doExecute(parameters);
            
            // 后置处理
            result = afterExecute(parameters, result);
            
            log.debug("执行工具 '{}' 成功", name);
            return result;
        } catch (ToolExecutionException e) {
            log.error("执行工具 '{}' 失败: {}", name, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("执行工具 '{}' 发生未知异常", name, e);
            throw new ToolExecutionException(name, "UNKNOWN_ERROR", "工具执行发生未知异常: " + e.getMessage(), e);
        }
    }
    
    /**
     * 执行工具的核心逻辑
     *
     * @param parameters 参数映射
     * @return 执行结果
     * @throws ToolExecutionException 执行异常
     */
    protected abstract String doExecute(Map<String, String> parameters) throws ToolExecutionException;
} 