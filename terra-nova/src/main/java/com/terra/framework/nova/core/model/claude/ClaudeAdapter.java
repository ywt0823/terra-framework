package com.terra.framework.nova.core.model.claude;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.core.exception.ErrorType;
import com.terra.framework.nova.core.exception.ModelException;
import com.terra.framework.nova.core.model.AbstractModelAdapter;
import com.terra.framework.nova.core.model.AuthProvider;
import com.terra.framework.nova.core.model.Message;
import com.terra.framework.nova.core.model.ModelRequest;
import com.terra.framework.nova.core.model.ModelResponse;
import com.terra.framework.nova.core.model.TokenUsage;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Claude模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class ClaudeAdapter extends AbstractModelAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    public ClaudeAdapter(ClaudeRequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }

    @Override
    public <T> T convertRequest(ModelRequest request, Class<T> vendorRequestType) {
        try {
            JSONObject claudeRequest = new JSONObject();

            // 设置模型
            String model = getModelName(request.getParameters());
            claudeRequest.put("model", model);

            // 设置是否流式输出
            if (request.isStream()) {
                claudeRequest.put("stream", true);
            }

            // 设置其他参数
            Map<String, Object> mappedParams = requestMappingStrategy.mapParameters(request.getParameters());
            for (Map.Entry<String, Object> entry : mappedParams.entrySet()) {
                // 跳过model参数，因为已经单独设置
                if (!"model".equals(entry.getKey())) {
                    claudeRequest.put(entry.getKey(), entry.getValue());
                }
            }

            // 根据请求类型设置消息
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                JSONArray messagesArray = convertMessages(request.getMessages());
                claudeRequest.put("messages", messagesArray);
            } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
                // 对于Claude，将提示词转换为消息
                JSONArray messagesArray = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", request.getPrompt());
                messagesArray.add(userMessage);
                claudeRequest.put("messages", messagesArray);
            }

            return (T) claudeRequest;
        } catch (Exception e) {
            log.error("转换Claude请求失败", e);
            throw new ModelException("转换Claude请求失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> ModelResponse convertResponse(T vendorResponse) {
        try {
            if (vendorResponse instanceof String) {
                return parseStringResponse((String) vendorResponse);
            } else if (vendorResponse instanceof JSONObject) {
                return parseJsonResponse((JSONObject) vendorResponse);
            } else {
                throw new IllegalArgumentException("不支持的响应类型: " + vendorResponse.getClass().getName());
            }
        } catch (Exception e) {
            log.error("转换Claude响应失败", e);
            throw new ModelException("转换Claude响应失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ModelException handleException(Exception vendorException) {
        try {
            if (vendorException.getMessage() != null && vendorException.getMessage().contains("\"error\":")) {
                JSONObject errorJson = JSON.parseObject(vendorException.getMessage());
                if (errorJson.containsKey("error")) {
                    JSONObject error = errorJson.getJSONObject("error");
                    String errorType = error.getString("type");
                    String errorMessage = error.getString("message");

                    ErrorType modelErrorType = mapErrorType(errorType);
                    return new ModelException(
                            "Claude API错误: " + errorMessage,
                            vendorException,
                            modelErrorType
                    );
                }
            }
        } catch (Exception e) {
            // 如果解析失败，使用默认处理
            log.debug("解析Claude错误失败，使用默认错误处理", e);
        }

        return super.handleException(vendorException);
    }

    /**
     * 从参数中获取模型名称
     *
     * @param parameters 参数
     * @return 模型名称
     */
    private String getModelName(Map<String, Object> parameters) {
        Object model = parameters.get("model");
        return model != null ? model.toString() : "claude-3-opus-20240229";
    }

    /**
     * 将消息列表转换为Claude消息格式
     *
     * @param messages 消息列表
     * @return Claude消息数组
     */
    private JSONArray convertMessages(List<Message> messages) {
        JSONArray messagesArray = new JSONArray();

        for (Message message : messages) {
            JSONObject claudeMessage = new JSONObject();

            // 转换角色
            switch (message.getRole()) {
                case SYSTEM:
                    claudeMessage.put("role", "system");
                    break;
                case USER:
                    claudeMessage.put("role", "user");
                    break;
                case ASSISTANT:
                    claudeMessage.put("role", "assistant");
                    break;
                default:
                    // Claude不支持function和tool角色，将其转换为用户角色
                    claudeMessage.put("role", "user");
            }

            // 设置内容
            claudeMessage.put("content", message.getContent());
            messagesArray.add(claudeMessage);
        }

        return messagesArray;
    }

    /**
     * 解析字符串响应
     *
     * @param response 响应字符串
     * @return 模型响应
     */
    private ModelResponse parseStringResponse(String response) {
        try {
            JSONObject jsonResponse = JSON.parseObject(response);
            return parseJsonResponse(jsonResponse);
        } catch (Exception e) {
            // 如果不是JSON，则直接作为内容返回
            ModelResponse modelResponse = new ModelResponse();
            modelResponse.setContent(response);
            return modelResponse;
        }
    }

    /**
     * 解析JSON响应
     *
     * @param jsonResponse JSON响应
     * @return 模型响应
     */
    private ModelResponse parseJsonResponse(JSONObject jsonResponse) {
        ModelResponse modelResponse = new ModelResponse();

        // 解析常规响应
        if (jsonResponse.containsKey("content")) {
            String content = jsonResponse.getString("content");
            modelResponse.setContent(content);
        } else if (jsonResponse.containsKey("delta") && jsonResponse.getJSONObject("delta").containsKey("text")) {
            // 处理流式响应
            modelResponse.setContent(jsonResponse.getJSONObject("delta").getString("text"));
        }

        // 设置响应ID
        modelResponse.setResponseId(jsonResponse.getString("id"));

        // 设置模型ID
        modelResponse.setModelId(jsonResponse.getString("model"));

        // 设置创建时间
        if (jsonResponse.containsKey("created_at")) {
            // Claude返回的时间戳可能是ISO格式的日期字符串
            String createdAtStr = jsonResponse.getString("created_at");
            try {
                // 简单处理，转换为毫秒时间戳
                long createdAt = System.currentTimeMillis();
                modelResponse.setCreatedAt(createdAt);
            } catch (Exception e) {
                modelResponse.setCreatedAt(System.currentTimeMillis());
            }
        } else {
            modelResponse.setCreatedAt(System.currentTimeMillis());
        }

        // 设置Token使用情况
        if (jsonResponse.containsKey("usage")) {
            JSONObject usage = jsonResponse.getJSONObject("usage");
            TokenUsage tokenUsage = TokenUsage.of(
                    usage.getIntValue("input_tokens"),
                    usage.getIntValue("output_tokens")
            );
            modelResponse.setTokenUsage(tokenUsage);
        }

        // 设置原始响应
        modelResponse.setRawResponse((Map<String, Object>) JSON.toJSON(jsonResponse));

        return modelResponse;
    }

    /**
     * 映射Claude错误类型到内部错误类型
     *
     * @param claudeErrorType Claude错误类型
     * @return 内部错误类型
     */
    private ErrorType mapErrorType(String claudeErrorType) {
        if (claudeErrorType == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        switch (claudeErrorType) {
            case "authentication_error":
            case "invalid_api_key":
            case "unauthorized":
                return ErrorType.AUTHENTICATION_ERROR;
            case "rate_limit_exceeded":
                return ErrorType.RATE_LIMIT_ERROR;
            case "context_length_exceeded":
            case "content_too_long":
                return ErrorType.CONTEXT_LENGTH_ERROR;
            case "invalid_request_error":
            case "bad_request":
                return ErrorType.INVALID_REQUEST_ERROR;
            case "server_error":
            case "internal_error":
                return ErrorType.SERVER_ERROR;
            case "model_not_found":
            case "model_unavailable":
                return ErrorType.MODEL_UNAVAILABLE_ERROR;
            case "content_policy_violation":
            case "content_filtered":
                return ErrorType.CONTENT_FILTER_ERROR;
            default:
                return ErrorType.UNKNOWN_ERROR;
        }
    }
}
