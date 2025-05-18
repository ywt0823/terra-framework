package com.terra.framework.nova.prompt.template.impl;

import com.terra.framework.nova.prompt.Prompt;
import com.terra.framework.nova.prompt.SimplePrompt;
import com.terra.framework.nova.prompt.template.PromptTemplate;
import com.terra.framework.nova.prompt.template.TemplateEngine;

import java.util.Map;
import java.util.Objects;

/**
 * 字符串提示词模板实现
 *
 * @author terra-nova
 */
public class StringPromptTemplate implements PromptTemplate {

    private final String template;
    private final TemplateEngine engine;

    /**
     * 构造函数
     *
     * @param template 模板字符串
     * @param engine 模板引擎
     */
    public StringPromptTemplate(String template, TemplateEngine engine) {
        this.template = Objects.requireNonNull(template, "Template cannot be null");
        this.engine = Objects.requireNonNull(engine, "Template engine cannot be null");
    }

    @Override
    public Prompt format(Map<String, Object> variables) {
        String content = render(variables);
        return new SimplePrompt(content, variables);
    }

    @Override
    public String render(Map<String, Object> variables) {
        return engine.render(template, variables);
    }

    /**
     * 获取原始模板字符串
     *
     * @return 模板字符串
     */
    public String getTemplate() {
        return template;
    }
}
