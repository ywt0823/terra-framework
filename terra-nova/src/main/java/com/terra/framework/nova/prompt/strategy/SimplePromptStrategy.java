package com.terra.framework.nova.prompt.strategy;

import com.terra.framework.nova.prompt.history.PromptHistory;
import lombok.Getter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 简单提示策略实现
 *
 * @author terra-nova
 */
public class SimplePromptStrategy implements PromptStrategy {
    
    /**
     * 策略名称
     */
    @Getter
    private final String name;
    
    /**
     * 提示模板
     */
    private final String promptTemplate;
    
    /**
     * 系统提示
     */
    private final String systemPrompt;
    
    /**
     * 最大历史消息数
     */
    private final int maxHistoryMessages;
    
    /**
     * 构造函数
     */
    public SimplePromptStrategy() {
        this("simple", "你是一个有帮助的AI助手。请回答以下问题：\n{{query}}", "请提供有用、准确且详细的回答。", 5);
    }
    
    /**
     * 构造函数
     *
     * @param name 策略名称
     * @param promptTemplate 提示模板
     * @param systemPrompt 系统提示
     * @param maxHistoryMessages 最大历史消息数
     */
    public SimplePromptStrategy(String name, String promptTemplate, String systemPrompt, int maxHistoryMessages) {
        this.name = name;
        this.promptTemplate = promptTemplate;
        this.systemPrompt = systemPrompt;
        this.maxHistoryMessages = maxHistoryMessages;
    }
    
    @Override
    public String generatePrompt(String userInput, Map<String, Object> context) {
        Map<String, Object> variables = new HashMap<>(context != null ? context : new HashMap<>());
        variables.put("query", userInput);
        
        return replaceVariables(promptTemplate, variables);
    }
    
    @Override
    public String generatePromptWithHistory(String userInput, PromptHistory history, Map<String, Object> context) {
        if (history == null) {
            return generatePrompt(userInput, context);
        }
        
        // 构建带历史记录的提示
        StringBuilder prompt = new StringBuilder();
        
        // 添加系统提示
        String sysPrompt = history.getSystemPrompt() != null ? history.getSystemPrompt() : systemPrompt;
        if (sysPrompt != null && !sysPrompt.isEmpty()) {
            prompt.append("系统：").append(sysPrompt).append("\n\n");
        }
        
        // 添加历史对话
        List<String> userMessages = history.getUserMessages();
        List<String> assistantMessages = history.getAssistantMessages();
        
        int historySize = Math.min(maxHistoryMessages, Math.min(userMessages.size(), assistantMessages.size()));
        int startIndex = Math.max(0, userMessages.size() - historySize);
        
        for (int i = startIndex; i < userMessages.size() - 1 && i < assistantMessages.size(); i++) {
            prompt.append("用户：").append(userMessages.get(i)).append("\n");
            prompt.append("助手：").append(assistantMessages.get(i)).append("\n\n");
        }
        
        // 添加当前问题
        prompt.append("用户：").append(userInput).append("\n");
        prompt.append("助手：");
        
        return prompt.toString();
    }
    
    /**
     * 替换模板中的变量
     *
     * @param template 模板
     * @param variables 变量映射
     * @return 替换后的字符串
     */
    private String replaceVariables(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        
        String result = template;
        for (Map.Entry<String, Object> entry : variables.entrySet()) {
            String placeholder = "{{" + entry.getKey() + "}}";
            String value = entry.getValue() != null ? entry.getValue().toString() : "";
            result = result.replace(placeholder, value);
        }
        
        return result;
    }
} 