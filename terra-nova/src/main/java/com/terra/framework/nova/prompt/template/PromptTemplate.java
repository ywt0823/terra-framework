package com.terra.framework.nova.prompt.template;

import com.terra.framework.nova.prompt.Prompt;

import java.util.Map;

/**
 * 提示词模板接口，用于格式化模板并创建提示词
 *
 * @author terra-nova
 */
public interface PromptTemplate {

    /**
     * 使用变量渲染模板并创建Prompt
     *
     * @param variables 模板变量
     * @return 渲染后的Prompt
     */
    Prompt format(Map<String, Object> variables);

    /**
     * 使用变量渲染模板并返回字符串
     *
     * @param variables 模板变量
     * @return 渲染后的字符串
     */
    String render(Map<String, Object> variables);
}
