package com.terra.framework.nova.llm.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 函数调用信息
 *
 * @author terra-nova
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FunctionCallInfo {
    /**
     * 要调用的函数名称
     */
    private String name;

    /**
     * 调用函数的参数，为JSON字符串
     */
    private String arguments;
} 