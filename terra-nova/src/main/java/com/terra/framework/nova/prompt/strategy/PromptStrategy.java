package com.terra.framework.nova.prompt.strategy;

import com.terra.framework.nova.prompt.history.PromptHistory;

import java.util.Map;

/**
 * 提示策略接口，用于实现不同的提示策略
 *
 * @author terra-nova
 */
public interface PromptStrategy {
    
    /**
     * 获取策略名称
     *
     * @return 策略名称
     */
    String getName();
    
    /**
     * 生成提示
     *
     * @param userInput 用户输入
     * @param context 上下文变量
     * @return 生成的提示
     */
    String generatePrompt(String userInput, Map<String, Object> context);
    
    /**
     * 基于历史记录生成提示
     *
     * @param userInput 用户输入
     * @param history 历史记录
     * @param context 上下文变量
     * @return 生成的提示
     */
    String generatePromptWithHistory(String userInput, PromptHistory history, Map<String, Object> context);
} 