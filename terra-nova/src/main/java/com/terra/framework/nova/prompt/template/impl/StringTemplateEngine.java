package com.terra.framework.nova.prompt.template.impl;

import com.terra.framework.nova.prompt.template.TemplateEngine;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 简单字符串模板引擎实现
 * 支持${var}格式的变量替换
 *
 * @author terra-nova
 */
public class StringTemplateEngine implements TemplateEngine {

    private static final Pattern VAR_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    @Override
    public String render(String template, Map<String, Object> variables) {
        if (template == null) {
            return "";
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        StringBuffer result = new StringBuffer();
        Matcher matcher = VAR_PATTERN.matcher(template);

        while (matcher.find()) {
            String varName = matcher.group(1);
            Object varValue = variables.get(varName);
            String replacement = varValue != null ? varValue.toString() : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }

        matcher.appendTail(result);
        return result.toString();
    }
}
