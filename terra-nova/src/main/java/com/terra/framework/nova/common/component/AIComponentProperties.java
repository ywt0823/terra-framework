package com.terra.framework.nova.common.component;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI组件属性配置
 *
 * @author terra-nova
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.component")
public class AIComponentProperties {
    
    /**
     * 是否启用组件系统
     */
    private boolean enabled = true;
    
    /**
     * 基础包路径
     */
    private String[] basePackages = {};
    
    /**
     * 是否自动注册组件
     */
    private boolean autoRegister = true;
    
    /**
     * 工具相关配置
     */
    private final Tool tool = new Tool();
    
    /**
     * 函数相关配置
     */
    private final Function function = new Function();
    
    /**
     * 工具配置类
     */
    @Data
    public static class Tool {
        /**
         * 是否启用工具功能
         */
        private boolean enabled = true;
        
        /**
         * 是否保留传统的@AITool注解支持
         * @deprecated 旧注解系统已被移除，此配置无效
         */
        @Deprecated
        private boolean legacyAnnotationSupport = false;
    }
    
    /**
     * 函数配置类
     */
    @Data
    public static class Function {
        /**
         * 是否启用函数功能
         */
        private boolean enabled = true;
        
        /**
         * 是否保留传统的@AIFunction注解支持
         * @deprecated 旧注解系统已被移除，此配置无效
         */
        @Deprecated
        private boolean legacyAnnotationSupport = false;
        
        /**
         * 是否验证参数类型
         */
        private boolean validateParameterTypes = true;
    }
} 