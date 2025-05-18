package com.terra.framework.nova.llm.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * 模型配置
 *
 * @author terra-nova
 */
@Data
@Builder
public class ModelConfig {

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 模型类型
     */
    private ModelType modelType;

    /**
     * API端点
     */
    private String endpoint;

    /**
     * 认证配置
     */
    private AuthConfig authConfig;

    /**
     * 默认参数
     */
    @Builder.Default
    private Map<String, Object> defaultParameters = new HashMap<>();

    /**
     * 重试配置
     */
    private RetryConfig retryConfig;

    /**
     * 超时时间（毫秒）
     */
    @Builder.Default
    private int timeout = 30000;

    /**
     * 是否支持流式输出
     */
    @Builder.Default
    private boolean streamSupport = true;

    /**
     * 添加默认参数
     *
     * @param key 参数名
     * @param value 参数值
     * @return 当前配置实例
     */
    public ModelConfig addDefaultParameter(String key, Object value) {
        this.defaultParameters.put(key, value);
        return this;
    }
}
