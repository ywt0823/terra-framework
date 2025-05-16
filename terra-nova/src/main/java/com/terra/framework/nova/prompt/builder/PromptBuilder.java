package com.terra.framework.nova.prompt.builder;

import com.terra.framework.nova.prompt.template.PromptTemplate;
import lombok.Getter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 提示构建器，用于构建和组装提示
 *
 * @author terra-nova
 */
public class PromptBuilder {

    /**
     * 提示模板
     */
    @Getter
    private final PromptTemplate template;

    /**
     * 变量映射
     */
    private final Map<String, Object> variables;

    /**
     * 系统消息
     */
    private String systemMessage;

    /**
     * 历史消息列表
     */
    private final List<Map<String, String>> historyMessages;

    /**
     * 构造函数
     *
     * @param template 提示模板
     */
    public PromptBuilder(PromptTemplate template) {
        this.template = template;
        this.variables = new HashMap<>();
        this.historyMessages = new ArrayList<>();
    }

    /**
     * 创建一个提示构建器
     *
     * @param template 提示模板
     * @return 提示构建器
     */
    public static PromptBuilder from(PromptTemplate template) {
        return new PromptBuilder(template);
    }

    /**
     * 创建一个提示构建器
     *
     * @param templateString 模板字符串
     * @return 提示构建器
     */
    public static PromptBuilder from(String templateString) {
        return new PromptBuilder(PromptTemplate.of(templateString));
    }

    /**
     * 添加变量
     *
     * @param name 变量名
     * @param value 变量值
     * @return 当前构建器
     */
    public PromptBuilder variable(String name, Object value) {
        this.variables.put(name, value);
        return this;
    }

    /**
     * 批量添加变量
     *
     * @param variables 变量映射
     * @return 当前构建器
     */
    public PromptBuilder variables(Map<String, Object> variables) {
        if (variables != null) {
            this.variables.putAll(variables);
        }
        return this;
    }

    /**
     * 设置系统消息
     *
     * @param systemMessage 系统消息
     * @return 当前构建器
     */
    public PromptBuilder systemMessage(String systemMessage) {
        this.systemMessage = systemMessage;
        return this;
    }

    /**
     * 添加用户消息到历史
     *
     * @param message 用户消息
     * @return 当前构建器
     */
    public PromptBuilder addUserMessage(String message) {
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("role", "user");
        messageMap.put("content", message);
        this.historyMessages.add(messageMap);
        return this;
    }

    /**
     * 添加助手消息到历史
     *
     * @param message 助手消息
     * @return 当前构建器
     */
    public PromptBuilder addAssistantMessage(String message) {
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("role", "assistant");
        messageMap.put("content", message);
        this.historyMessages.add(messageMap);
        return this;
    }

    /**
     * 添加系统消息到历史
     *
     * @param message 系统消息
     * @return 当前构建器
     */
    public PromptBuilder addSystemMessage(String message) {
        Map<String, String> messageMap = new HashMap<>();
        messageMap.put("role", "system");
        messageMap.put("content", message);
        this.historyMessages.add(messageMap);
        return this;
    }

    /**
     * 构建完成提示
     *
     * @return 格式化后的提示字符串
     */
    public String build() {
        return template.format(variables);
    }

    /**
     * 构建聊天提示
     *
     * @return 聊天消息列表
     */
    public List<Map<String, String>> buildChatMessages() {
        List<Map<String, String>> messages = new ArrayList<>();
        
        // 添加系统消息
        if (systemMessage != null && !systemMessage.isEmpty()) {
            Map<String, String> sysMsg = new HashMap<>();
            sysMsg.put("role", "system");
            sysMsg.put("content", systemMessage);
            messages.add(sysMsg);
        }
        
        // 添加历史消息
        messages.addAll(historyMessages);
        
        // 添加最后的用户消息（从模板生成）
        String userPrompt = build();
        if (userPrompt != null && !userPrompt.isEmpty()) {
            Map<String, String> userMsg = new HashMap<>();
            userMsg.put("role", "user");
            userMsg.put("content", userPrompt);
            messages.add(userMsg);
        }
        
        return messages;
    }
    
    /**
     * 获取所有变量
     *
     * @return 变量映射
     */
    public Map<String, Object> getVariables() {
        return new HashMap<>(variables);
    }
    
    /**
     * 获取历史消息
     *
     * @return 历史消息列表
     */
    public List<Map<String, String>> getHistoryMessages() {
        return new ArrayList<>(historyMessages);
    }
} 