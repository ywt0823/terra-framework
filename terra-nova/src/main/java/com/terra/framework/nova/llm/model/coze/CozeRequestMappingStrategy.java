package com.terra.framework.nova.llm.model.coze;

import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.MessageRole;
import com.terra.framework.nova.llm.model.RequestMappingStrategy;
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
public class CozeRequestMappingStrategy implements RequestMappingStrategy {

    /**
     * 参数映射表
     */
    private static final Map<String, String> PARAMETER_MAPPING = new HashMap<>();

    static {
        // 初始化参数映射
        PARAMETER_MAPPING.put("temperature", "temperature");
        PARAMETER_MAPPING.put("topP", "top_p");
        PARAMETER_MAPPING.put("maxTokens", "max_tokens");
        PARAMETER_MAPPING.put("presencePenalty", "presence_penalty");
        PARAMETER_MAPPING.put("frequencyPenalty", "frequency_penalty");
        PARAMETER_MAPPING.put("stop", "stop");
        PARAMETER_MAPPING.put("n", "n");
        PARAMETER_MAPPING.put("logitBias", "logit_bias");
        PARAMETER_MAPPING.put("user", "user");
    }

    @Override
    public Map<String, Object> mapParameters(Map<String, Object> parameters) {
        Map<String, Object> mappedParams = new HashMap<>();

        // 遍历参数映射表
        for (Map.Entry<String, String> entry : PARAMETER_MAPPING.entrySet()) {
            String sourceKey = entry.getKey();
            String targetKey = entry.getValue();

            // 如果参数存在，则添加到映射结果中
            if (parameters.containsKey(sourceKey)) {
                mappedParams.put(targetKey, parameters.get(sourceKey));
            }
        }

        // 处理特殊参数
        if (parameters.containsKey("model")) {
            mappedParams.put("model", parameters.get("model"));
        }

        return mappedParams;
    }

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

    private String mapRole(MessageRole role) {
        return switch (role) {
            case USER -> "user";
            case ASSISTANT -> "assistant";
            case SYSTEM -> "system";
            case FUNCTION -> "function";
            default -> throw new IllegalArgumentException("Unsupported role: " + role);
        };
    }
}
