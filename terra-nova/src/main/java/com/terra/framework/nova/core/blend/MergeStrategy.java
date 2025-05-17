package com.terra.framework.nova.core.blend;

/**
 * 合并策略枚举
 *
 * @author terra-nova
 */
public enum MergeStrategy {

    /**
     * 取最长的结果
     */
    LONGEST,

    /**
     * 取最短的结果
     */
    SHORTEST,

    /**
     * 投票选择（适用于分类任务）
     */
    VOTING,

    /**
     * 串联所有结果
     */
    CONCATENATE,

    /**
     * 根据权重合并
     */
    WEIGHTED,

    /**
     * 随机选择一个结果
     */
    RANDOM,

    /**
     * 使用第一个成功的结果
     */
    FIRST_SUCCESS,

    /**
     * 分析结果质量选择最佳
     */
    QUALITY_BASED,

    /**
     * 合并为列表（每个模型的结果作为一个列表项）
     */
    LIST_FORMAT,

    /**
     * 交错合并（交替使用每个模型的部分结果）
     */
    INTERLEAVE
}
