package com.terra.framework.nova.model;

import java.util.Map;
import lombok.Data;

/**
 * 模型响应结果
 *
 * @author terra-nova
 */
@Data
public class ModelResponse {

    /**
     * 生成的内容
     */
    private String content;

    /**
     * Token使用情况
     */
    private TokenUsage tokenUsage;

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 响应ID
     */
    private String responseId;

    /**
     * 创建时间戳
     */
    private long createdAt;

    /**
     * 厂商原始响应，用于扩展信息获取
     */
    private Map<String, Object> rawResponse;
}
