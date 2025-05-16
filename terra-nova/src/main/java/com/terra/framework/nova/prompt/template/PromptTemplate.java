package com.terra.framework.nova.prompt.template;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 提示模板，用于定义和构建提示
 *
 * @author terra-nova
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PromptTemplate {

    /**
     * 模板ID
     */
    private String id;

    /**
     * 模板名称
     */
    private String name;

    /**
     * 模板描述
     */
    private String description;

    /**
     * 模板内容
     */
    private String template;

    /**
     * 模板类型 (如: chat, completion)
     */
    private String type;

    /**
     * 模板变量正则表达式
     */
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([\\w.]+)\\s*\\}\\}");

    /**
     * 创建一个简单的提示模板
     *
     * @param template 模板内容
     * @return 提示模板
     */
    public static PromptTemplate of(String template) {
        return PromptTemplate.builder()
                .id(UUID.randomUUID().toString())
                .template(template)
                .type("completion")
                .build();
    }

    /**
     * 创建一个带名称的提示模板
     *
     * @param name 模板名称
     * @param template 模板内容
     * @return 提示模板
     */
    public static PromptTemplate of(String name, String template) {
        return PromptTemplate.builder()
                .id(UUID.randomUUID().toString())
                .name(name)
                .template(template)
                .type("completion")
                .build();
    }

    /**
     * 用变量值替换模板中的变量占位符
     *
     * @param variables 变量映射
     * @return 替换后的字符串
     */
    public String format(Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return "";
        }

        if (variables == null || variables.isEmpty()) {
            return template;
        }

        String result = template;
        Matcher matcher = VARIABLE_PATTERN.matcher(result);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object value = variables.get(variableName);
            String replacement = (value != null) ? value.toString() : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }

    /**
     * 提取模板中的变量名
     *
     * @return 变量名集合
     */
    public Map<String, String> extractVariables() {
        Map<String, String> variables = new HashMap<>();
        
        if (template == null || template.isEmpty()) {
            return variables;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        while (matcher.find()) {
            String variableName = matcher.group(1);
            variables.put(variableName, "");
        }

        return variables;
    }
} 