package com.terra.framework.nova.llm.model;

import java.util.List;
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
     * 当模型返回函数调用时，该字段通常为null
     */
    private String content;

    /**
     * 模型请求调用的工具列表
     * 当模型决定调用工具时使用
     */
    private List<ToolCall> toolCalls;
    
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
    
    /**
     * 判断响应是否包含工具调用
     *
     * @return 如果响应包含工具调用则返回true
     */
    public boolean hasToolCalls() {
        return toolCalls != null && !toolCalls.isEmpty();
    }
}
