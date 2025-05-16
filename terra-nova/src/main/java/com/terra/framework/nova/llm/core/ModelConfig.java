package com.terra.framework.nova.llm.core;

import lombok.Data;
import lombok.Builder;

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
     * 温度参数 (0.0-1.0)
     */
    private Double temperature;

    /**
     * 最大token数
     */
    private Integer maxTokens;

    /**
     * 额外参数
     */
    private Map<String, Object> extraParams;
}
