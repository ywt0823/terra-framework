package com.terra.framework.nova.llm.model.coze;

import com.terra.framework.nova.llm.model.AbstractRequestMappingStrategy;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.MessageRole;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Coze请求参数映射策略
 *
 * @author terra-nova
 */
@Slf4j
public class CozeRequestMappingStrategy extends AbstractRequestMappingStrategy {

    @Override
    protected void initVendorSpecificParamMapping() {
        // Coze特有的参数映射
        paramMapping.put("topP", "top_p");
        paramMapping.put("maxTokens", "max_tokens");
        paramMapping.put("presencePenalty", "presence_penalty");
        paramMapping.put("frequencyPenalty", "frequency_penalty");
        paramMapping.put("logitBias", "logit_bias");
    }

    /**
     * 将通用请求转换为Coze聊天请求
     *
     * @param messages 消息列表
     * @param parameters 请求参数
     * @return Coze聊天请求
     */
    public Map<String, Object> mapChatRequest(List<Message> messages, Map<String, Object> parameters) {
        Map<String, Object> request = new HashMap<>();

        // 转换消息格式
        List<Map<String, String>> cozeMessages = messages.stream()
            .map(message -> {
                Map<String, String> cozeMessage = new HashMap<>();
                cozeMessage.put("role", mapRole(message.getRole()));
                cozeMessage.put("content", message.getContent());
                return cozeMessage;
            })
            .collect(Collectors.toList());

        request.put("messages", cozeMessages);
        request.putAll(mapParameters(parameters));

        return request;
    }

    /**
     * 将提示文本转换为Coze补全请求
     *
     * @param prompt 提示文本
     * @param parameters 请求参数
     * @return Coze补全请求
     */
    public Map<String, Object> mapCompletionRequest(String prompt, Map<String, Object> parameters) {
        Map<String, Object> request = new HashMap<>();

        // 构建单条消息
        Map<String, String> message = new HashMap<>();
        message.put("role", "user");
        message.put("content", prompt);

        request.put("messages", List.of(message));
        request.putAll(mapParameters(parameters));

        return request;
    }

    /**
     * 映射角色
     *
     * @param role 角色
     * @return Coze角色字符串
     */
    private String mapRole(MessageRole role) {
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
            case FUNCTION -> "function";
            default -> throw new IllegalArgumentException("Unsupported role: " + role);
        };
    }

    @Override
    protected String getDefaultModelName() {
        return "gpt-4";
    }
}
