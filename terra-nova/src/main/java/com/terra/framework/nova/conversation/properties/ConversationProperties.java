package com.terra.framework.nova.conversation.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "terra.nova.conversation")
public class ConversationProperties {
    /**
     * 是否启用会话功能
     */
    private boolean enabled = true;

    /**
     * 会话ID生成策略
     */
    private String idGenerator = "uuid";

    /**
     * 最大会话消息数
     */
    private int maxMessagesPerConversation = 100;
} 