package com.terra.framework.nova.prompt;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 简单提示词实现
 *
 * @author terra-nova
 */
public class SimplePrompt implements Prompt {

    private final String content;
    private final Map<String, Object> variables;

    /**
     * 构造函数
     *
     * @param content 提示词内容
     */
    public SimplePrompt(String content) {
        this(content, Collections.emptyMap());
    }

    /**
     * 构造函数
     *
     * @param content 提示词内容
     * @param variables 提示词变量
     */
    public SimplePrompt(String content, Map<String, Object> variables) {
        this.content = Objects.requireNonNull(content, "Prompt content cannot be null");
        this.variables = new HashMap<>(variables != null ? variables : Collections.emptyMap());
    }

    @Override
    public String getContent() {
        return content;
    }

    @Override
    public Map<String, Object> getVariables() {
        return Collections.unmodifiableMap(variables);
    }

    @Override
    public String toString() {
        return content;
    }
}
