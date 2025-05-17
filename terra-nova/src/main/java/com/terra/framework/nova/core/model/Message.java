package com.terra.framework.nova.core.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 对话消息
 *
 * @author terra-nova
 */
@Data
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
}
