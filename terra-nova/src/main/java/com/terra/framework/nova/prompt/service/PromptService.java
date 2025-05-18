package com.terra.framework.nova.prompt.service;

import com.terra.framework.nova.prompt.Prompt;

import java.util.Map;

/**
 * 提示词服务接口
 *
 * @author terra-nova
 */
public interface PromptService {
    
    /**
     * 使用变量渲染模板
     *
     * @param templateId 模板ID
     * @param variables 模板变量
     * @return 渲染后的内容
     */
    String render(String templateId, Map<String, Object> variables);
    
    /**
     * 创建提示词
     *
     * @param templateId 模板ID
     * @param variables 模板变量
     * @return 创建的提示词
     */
    Prompt createPrompt(String templateId, Map<String, Object> variables);
} 