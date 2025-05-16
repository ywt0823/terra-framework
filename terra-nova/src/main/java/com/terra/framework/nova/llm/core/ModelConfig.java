package com.terra.framework.nova.llm.core;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;

/**
 * 模型配置类
 */
@Data
@Builder
public class ModelConfig {
    /**
     * 模型类型
     */
    private ModelType type;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API端点
     */
    private String apiEndpoint;

    /**
     * 模型名称
     */
    private String modelName;

    /**
     * 温度参数 (0.0-1.0)
     */
    @Builder.Default
    private Double temperature = 0.7;

    /**
     * 最大token数
     */
    @Builder.Default
    private Integer maxTokens = 2048;

    /**
     * 超时时间(毫秒)
     */
    @Builder.Default
    private Long timeoutMs = 30000L;

    /**
     * 重试次数
     */
    @Builder.Default
    private Integer maxRetries = 3;

    /**
     * 是否启用流式响应
     */
    @Builder.Default
    private Boolean streamEnabled = false;

    /**
     * 额外参数
     */
    @Builder.Default
    private Map<String, Object> extraParams = new HashMap<>();

    /**
     * 获取参数值
     */
    public Object getParam(String key) {
        return extraParams.get(key);
    }

    /**
     * 获取参数值,如果不存在则返回默认值
     */
    public Object getParam(String key, Object defaultValue) {
        return extraParams.getOrDefault(key, defaultValue);
    }

    /**
     * 设置参数值
     */
    public void setParam(String key, Object value) {
        extraParams.put(key, value);
    }
}
