package com.terra.framework.nova.llm.manager;

import lombok.Data;
import lombok.Builder;

import java.util.Map;
import java.util.HashMap;

/**
 * Chain配置类
 */
@Data
@Builder
public class ChainConfig {

    /**
     * Chain类型
     */
    private ChainType type;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 参数映射
     */
    @Builder.Default
    private Map<String, String> params = new HashMap<>();

    /**
     * 获取参数值
     *
     * @param key 参数键
     * @return 参数值
     */
    public String getParam(String key) {
        return params.get(key);
    }

    /**
     * 获取参数值，如果不存在则返回默认值
     *
     * @param key 参数键
     * @param defaultValue 默认值
     * @return 参数值
     */
    public String getParam(String key, String defaultValue) {
        return params.getOrDefault(key, defaultValue);
    }
}

