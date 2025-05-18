package com.terra.framework.nova.llm.model;

import lombok.Data;

/**
 * Token使用情况统计
 *
 * @author terra-nova
 */
@Data
public class TokenUsage {

    /**
     * 提示词使用的Token数量
     */
    private int promptTokens;

    /**
     * 完成结果使用的Token数量
     */
    private int completionTokens;

    /**
     * 总Token使用量
     */
    private int totalTokens;

    /**
     * 创建TokenUsage对象
     *
     * @param promptTokens 提示词Token数
     * @param completionTokens 完成结果Token数
     * @return TokenUsage实例
     */
    public static TokenUsage of(int promptTokens, int completionTokens) {
        TokenUsage usage = new TokenUsage();
        usage.setPromptTokens(promptTokens);
        usage.setCompletionTokens(completionTokens);
        usage.setTotalTokens(promptTokens + completionTokens);
        return usage;
    }
}
