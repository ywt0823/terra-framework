package com.terra.framework.nova.prompt;

import java.util.Map;

/**
 * Prompt接口，表示一个可用于AI模型的提示词
 *
 * @author terra-nova
 */
public interface Prompt {
    
    /**
     * 获取提示词内容
     *
     * @return 提示词内容
     */
    String getContent();
    
    /**
     * 获取提示词变量
     *
     * @return 提示词变量
     */
    Map<String, Object> getVariables();
} 