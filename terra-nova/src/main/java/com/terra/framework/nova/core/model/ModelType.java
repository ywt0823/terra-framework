package com.terra.framework.nova.core.model;

/**
 * 模型类型枚举
 *
 * @author terra-nova
 */
public enum ModelType {
    /**
     * OpenAI模型
     */
    OPENAI("openai"),

    /**
     * Claude模型
     */
    CLAUDE("claude"),

    /**
     * 文心一言模型
     */
    WENXIN("wenxin"),

    /**
     * 通义千问模型
     */
    TONGYI("tongyi"),

    /**
     * DeepSeek模型
     */
    DEEPSEEK("deepseek"),

    /**
     * Dify平台模型
     */
    DIFY("dify"),

    /**
     * Ollama本地模型
     */
    OLLAMA("ollama"),


    ;

    private final String value;

    ModelType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
    }
