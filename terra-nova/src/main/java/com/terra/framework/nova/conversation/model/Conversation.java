package com.terra.framework.nova.conversation.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Data
@Builder
public class Conversation {
    private String id;
    private String userId;
    private String title;
    private ConversationStatus status;
    private List<ConversationMessage> messages;
    private Map<String, Object> metadata;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}
