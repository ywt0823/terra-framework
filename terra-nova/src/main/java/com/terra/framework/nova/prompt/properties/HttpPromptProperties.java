package com.terra.framework.nova.prompt.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HTTP提示词模板加载器配置属性
 *
 * @author terra-nova
 */
@ConfigurationProperties(prefix = "terra.nova.prompt.http")
public class HttpPromptProperties {

    /**
     * 是否启用HTTP提示词模板加载器
     */
    private boolean enabled = false;
    
    /**
     * HTTP提示词模板服务基础URL
     */
    private String baseUrl = "http://localhost:8080";
    
    /**
     * HTTP提示词模板路径模式，{id}会被替换为模板ID
     */
    private String templatePath = "/templates/{id}";
    
    /**
     * HTTP提示词模板缓存有效期（分钟）
     */
    private long cacheTtlMinutes = 5;
    
    /**
     * HTTP提示词模板服务认证令牌
     */
    private String authToken;
    
    /**
     * HTTP提示词模板服务认证头名称
     */
    private String authHeaderName = "Authorization";

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getTemplatePath() {
        return templatePath;
    }

    public void setTemplatePath(String templatePath) {
        this.templatePath = templatePath;
    }

    public long getCacheTtlMinutes() {
        return cacheTtlMinutes;
    }

    public void setCacheTtlMinutes(long cacheTtlMinutes) {
        this.cacheTtlMinutes = cacheTtlMinutes;
    }

    public String getAuthToken() {
        return authToken;
    }

    public void setAuthToken(String authToken) {
        this.authToken = authToken;
    }

    public String getAuthHeaderName() {
        return authHeaderName;
    }

    public void setAuthHeaderName(String authHeaderName) {
        this.authHeaderName = authHeaderName;
    }
} 