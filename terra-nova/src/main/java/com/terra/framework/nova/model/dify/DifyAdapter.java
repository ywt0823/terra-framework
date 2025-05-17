package com.terra.framework.nova.model.dify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.exception.ErrorType;
import com.terra.framework.nova.exception.ModelException;
import com.terra.framework.nova.model.AbstractModelAdapter;
import com.terra.framework.nova.model.AuthProvider;
import com.terra.framework.nova.model.Message;
import com.terra.framework.nova.model.ModelRequest;
import com.terra.framework.nova.model.ModelResponse;
import com.terra.framework.nova.model.TokenUsage;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Dify模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class DifyAdapter extends AbstractModelAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    public DifyAdapter(DifyRequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }

    @Override
    public <T> T convertRequest(ModelRequest request, Class<T> vendorRequestType) {
        try {
            JSONObject difyRequest = new JSONObject();

            // Dify API不需要在请求中明确指定模型，而是在API服务端进行配置

            // 设置是否流式输出
            if (request.isStream()) {
                difyRequest.put("stream", true);
            }

            // 设置其他参数
            Map<String, Object> mappedParams = requestMappingStrategy.mapParameters(request.getParameters());
            for (Map.Entry<String, Object> entry : mappedParams.entrySet()) {
                difyRequest.put(entry.getKey(), entry.getValue());
            }

            // 根据请求类型设置消息或提示词
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                JSONArray messagesArray = convertMessages(request.getMessages());
                difyRequest.put("inputs", new JSONObject()); // 默认空的inputs
                difyRequest.put("query", null); // 不使用query方式
                difyRequest.put("messages", messagesArray);
            } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
                // 对于提示词，使用query模式
                difyRequest.put("inputs", new JSONObject()); // 默认空的inputs
                difyRequest.put("query", request.getPrompt());
                // 不设置消息
                difyRequest.put("messages", new JSONArray());
            }

            // 设置对话模式（用户自定义选项）
            Object conversationId = request.getParameters().get("conversation_id");
            if (conversationId != null) {
                difyRequest.put("conversation_id", conversationId.toString());
            }

            Object user = request.getParameters().get("user");
            if (user != null) {
                difyRequest.put("user", user.toString());
            }

            return (T) difyRequest;
        } catch (Exception e) {
            log.error("转换Dify请求失败", e);
            throw new ModelException("转换Dify请求失败: " + e.getMessage(), e);
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
            log.error("转换Dify响应失败", e);
            throw new ModelException("转换Dify响应失败: " + e.getMessage(), e);
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
                            "Dify API错误: " + errorMessage,
                            vendorException,
                            modelErrorType
                    );
                }
            }
        } catch (Exception e) {
            // 如果解析失败，使用默认处理
            log.debug("解析Dify错误失败，使用默认错误处理", e);
        }

        return super.handleException(vendorException);
    }

    /**
     * 将消息列表转换为Dify消息格式
     *
     * @param messages 消息列表
     * @return Dify消息数组
     */
    private JSONArray convertMessages(List<Message> messages) {
        JSONArray messagesArray = new JSONArray();

        for (Message message : messages) {
            JSONObject difyMessage = new JSONObject();

            // 转换角色
            switch (message.getRole()) {
                case SYSTEM:
                    // Dify没有system角色，但可以作为user的特殊消息
                    difyMessage.put("role", "user");
                    difyMessage.put("content", "[系统提示] " + message.getContent());
                    break;
                case USER:
                    difyMessage.put("role", "user");
                    difyMessage.put("content", message.getContent());
                    break;
                case ASSISTANT:
                    difyMessage.put("role", "assistant");
                    difyMessage.put("content", message.getContent());
                    break;
                case FUNCTION:
                case TOOL:
                    // Dify可能不直接支持function和tool角色，作为assistant消息处理
                    difyMessage.put("role", "assistant");
                    difyMessage.put("content", "工具/函数调用结果: " + message.getContent());
                    break;
                default:
                    difyMessage.put("role", "user");
                    difyMessage.put("content", message.getContent());
            }

            messagesArray.add(difyMessage);
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

        // 解析普通响应
        if (jsonResponse.containsKey("answer")) {
            String content = jsonResponse.getString("answer");
            modelResponse.setContent(content);
        } else if (jsonResponse.containsKey("event") && "message".equals(jsonResponse.getString("event"))) {
            // 解析流式响应
            if (jsonResponse.containsKey("answer")) {
                modelResponse.setContent(jsonResponse.getString("answer"));
            }
        }

        // 设置响应ID
        if (jsonResponse.containsKey("conversation_id")) {
            modelResponse.setResponseId(jsonResponse.getString("conversation_id"));
        } else if (jsonResponse.containsKey("id")) {
            modelResponse.setResponseId(jsonResponse.getString("id"));
        }

        // 尝试获取模型ID
        if (jsonResponse.containsKey("model")) {
            modelResponse.setModelId(jsonResponse.getString("model"));
        }

        // 设置创建时间
        modelResponse.setCreatedAt(System.currentTimeMillis());

        // 设置Token使用情况
        if (jsonResponse.containsKey("usage")) {
            JSONObject usage = jsonResponse.getJSONObject("usage");
            TokenUsage tokenUsage = TokenUsage.of(
                    usage.getIntValue("prompt_tokens") + usage.getIntValue("prompt_tokens"),
                    usage.getIntValue("completion_tokens")
            );
            modelResponse.setTokenUsage(tokenUsage);
        }

        // 设置原始响应
        modelResponse.setRawResponse((Map<String, Object>) JSON.toJSON(jsonResponse));

        return modelResponse;
    }

    /**
     * 映射Dify错误类型到内部错误类型
     *
     * @param difyErrorType Dify错误类型
     * @return 内部错误类型
     */
    private ErrorType mapErrorType(String difyErrorType) {
        if (difyErrorType == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        switch (difyErrorType) {
            case "authentication_error":
            case "unauthorized":
                return ErrorType.AUTHENTICATION_ERROR;
            case "rate_limit_error":
                return ErrorType.RATE_LIMIT_ERROR;
            case "context_length_exceeded":
                return ErrorType.CONTEXT_LENGTH_ERROR;
            case "invalid_request_error":
                return ErrorType.INVALID_REQUEST_ERROR;
            case "service_unavailable":
            case "internal_server_error":
                return ErrorType.SERVER_ERROR;
            case "model_error":
                return ErrorType.MODEL_UNAVAILABLE_ERROR;
            case "content_filter":
                return ErrorType.CONTENT_FILTER_ERROR;
            default:
                return ErrorType.UNKNOWN_ERROR;
        }
    }
}
