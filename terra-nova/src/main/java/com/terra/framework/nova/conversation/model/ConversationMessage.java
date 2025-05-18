package com.terra.framework.nova.conversation.model;

import com.terra.framework.nova.llm.model.MessageRole;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Builder
public class ConversationMessage {
    private String id;
    private String conversationId;
    private MessageRole role;
    private String content;
    private Map<String, Object> metadata;
    private LocalDateTime createTime;
} 