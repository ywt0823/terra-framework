package com.terra.framework.nova.core.model;

import lombok.Builder;
import lombok.Data;

/**
 * 模型信息
 *
 * @author terra-nova
 */
@Data
@Builder
public class ModelInfo {

    /**
     * 模型ID
     */
    private String modelId;

    /**
     * 模型类型
     */
    private ModelType modelType;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型版本
     */
    private String version;

    /**
     * 模型厂商
     */
    private String vendor;

    /**
     * 模型描述
     */
    private String description;

    /**
     * 模型最大输入Token数
     */
    private int maxInputTokens;

    /**
     * 模型最大输出Token数
     */
    private int maxOutputTokens;

    /**
     * 是否支持流式输出
     */
    private boolean streamSupported;

    /**
     * 是否支持对话模式
     */
    private boolean chatSupported;
}
