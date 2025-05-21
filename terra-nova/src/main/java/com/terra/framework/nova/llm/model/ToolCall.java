package com.terra.framework.nova.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 工具调用，表示模型请求调用的工具
 *
 * @author terra-nova
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ToolCall {
    /**
     * 工具调用ID，在后续对话中需要引用此ID
     */
    private String id;

    /**
     * 工具类型，目前仅支持"function"
     */
    private String type;

    /**
     * 函数调用信息，当type为"function"时使用
     */
    private FunctionCallInfo function;
} 