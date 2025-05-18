package com.terra.framework.nova.prompt.template;

import java.util.Map;

/**
 * 模板引擎接口，用于渲染模板
 *
 * @author terra-nova
 */
public interface TemplateEngine {

    /**
     * 使用变量渲染模板
     *
     * @param template 模板字符串
     * @param variables 模板变量
     * @return 渲染后的内容
     */
    String render(String template, Map<String, Object> variables);
}
