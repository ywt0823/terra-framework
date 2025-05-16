package com.terra.framework.nova.model.router;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * 模型路由上下文
 *
 * @author terra-nova
 */
@Data
public class RoutingContext {
    
    /**
     * 请求ID
     */
    private String requestId;
    
    /**
     * 提示词
     */
    private String prompt;
    
    /**
     * 首选模型，如果指定则尝试使用此模型
     */
    private String preferredModel;
    
    /**
     * 首选提供商，如果指定则尝试使用此提供商
     */
    private String preferredProvider;
    
    /**
     * 是否允许回退到其他模型
     */
    private boolean fallbackEnabled = true;
    
    /**
     * 特殊需求，如高精度、低延迟等
     */
    private Map<String, Object> requirements = new HashMap<>();
    
    /**
     * 构造函数
     */
    public RoutingContext() {
    }
    
    /**
     * 构造函数
     *
     * @param requestId 请求ID
     * @param prompt    提示词
     */
    public RoutingContext(String requestId, String prompt) {
        this.requestId = requestId;
        this.prompt = prompt;
    }
    
    /**
     * 构造函数
     *
     * @param requestId        请求ID
     * @param prompt           提示词
     * @param preferredModel   首选模型
     * @param preferredProvider 首选提供商
     */
    public RoutingContext(String requestId, String prompt, String preferredModel, String preferredProvider) {
        this.requestId = requestId;
        this.prompt = prompt;
        this.preferredModel = preferredModel;
        this.preferredProvider = preferredProvider;
    }
    
    /**
     * 添加要求
     *
     * @param key   要求键
     * @param value 要求值
     * @return this
     */
    public RoutingContext addRequirement(String key, Object value) {
        this.requirements.put(key, value);
        return this;
    }
} 