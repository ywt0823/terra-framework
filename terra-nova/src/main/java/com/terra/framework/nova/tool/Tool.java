package com.terra.framework.nova.tool;

import java.util.Map;

/**
 * 工具接口，定义了可被LLM调用的工具
 *
 * @author terra-nova
 */
public interface Tool {
    
    /**
     * 获取工具名称
     *
     * @return 工具名称
     */
    String getName();
    
    /**
     * 获取工具描述
     *
     * @return 工具描述
     */
    String getDescription();
    
    /**
     * 获取工具参数描述
     *
     * @return 参数描述
     */
    Map<String, ParameterDescription> getParameterDescriptions();
    
    /**
     * 执行工具
     *
     * @param parameters 工具参数
     * @return 执行结果
     * @throws ToolExecutionException 工具执行异常
     */
    String execute(Map<String, String> parameters) throws ToolExecutionException;
    
    /**
     * 获取工具类别
     *
     * @return 工具类别
     */
    default String getCategory() {
        return "general";
    }
    
    /**
     * 工具是否需要认证
     *
     * @return 是否需要认证
     */
    default boolean requiresAuthentication() {
        return false;
    }
    
    /**
     * 工具是否异步执行
     *
     * @return 是否异步执行
     */
    default boolean isAsync() {
        return false;
    }
    
    /**
     * 参数描述类
     */
    class ParameterDescription {
        /**
         * 参数名称
         */
        private String name;
        
        /**
         * 参数描述
         */
        private String description;
        
        /**
         * 参数类型
         */
        private String type;
        
        /**
         * 是否必须
         */
        private boolean required;
        
        /**
         * 默认值
         */
        private String defaultValue;
        
        public ParameterDescription(String name, String description, String type, boolean required) {
            this(name, description, type, required, null);
        }
        
        public ParameterDescription(String name, String description, String type, boolean required, String defaultValue) {
            this.name = name;
            this.description = description;
            this.type = type;
            this.required = required;
            this.defaultValue = defaultValue;
        }
        
        public String getName() {
            return name;
        }
        
        public String getDescription() {
            return description;
        }
        
        public String getType() {
            return type;
        }
        
        public boolean isRequired() {
            return required;
        }
        
        public String getDefaultValue() {
            return defaultValue;
        }
    }
} 