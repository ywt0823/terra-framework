package com.terra.framework.nova.llm.model;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息
 *
 * @author terra-nova
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Message {

    /**
     * 消息角色
     */
    private MessageRole role;

    /**
     * 消息内容
     */
    private String content;
    
    /**
     * 函数或工具名称
     * 当角色为FUNCTION或TOOL时使用
     */
    private String name;
    
    /**
     * 工具调用ID
     * 当角色为TOOL时必须提供，用于关联工具调用响应
     */
    private String toolCallId;
    
    /**
     * 工具调用列表
     * 当角色为ASSISTANT且调用工具时使用
     */
    private List<ToolCall> toolCalls;
    
    /**
     * 简单构造函数
     *
     * @param role 角色
     * @param content 内容
     */
    public Message(MessageRole role, String content) {
        this.role = role;
        this.content = content;
    }

    /**
     * 创建系统消息
     *
     * @param content 内容
     * @return 系统消息
     */
    public static Message ofSystem(String content) {
        return new Message(MessageRole.SYSTEM, content);
    }

    /**
     * 创建用户消息
     *
     * @param content 内容
     * @return 用户消息
     */
    public static Message ofUser(String content) {
        return new Message(MessageRole.USER, content);
    }

    /**
     * 创建助手消息
     *
     * @param content 内容
     * @return 助手消息
     */
    public static Message ofAssistant(String content) {
        return new Message(MessageRole.ASSISTANT, content);
    }
    
    /**
     * 创建工具响应消息
     *
     * @param content 工具执行结果
     * @param name 工具名称
     * @param toolCallId 工具调用ID
     * @return 工具响应消息
     */
    public static Message ofTool(String content, String name, String toolCallId) {
        return Message.builder()
            .role(MessageRole.TOOL)
            .content(content)
            .name(name)
            .toolCallId(toolCallId)
            .build();
    }
    
    /**
     * 创建带工具调用的助手消息
     *
     * @param toolCalls 工具调用列表
     * @return 助手消息
     */
    public static Message ofAssistantWithToolCalls(List<ToolCall> toolCalls) {
        return Message.builder()
            .role(MessageRole.ASSISTANT)
            .content(null) // 工具调用消息通常内容为空
            .toolCalls(toolCalls)
            .build();
    }
}
