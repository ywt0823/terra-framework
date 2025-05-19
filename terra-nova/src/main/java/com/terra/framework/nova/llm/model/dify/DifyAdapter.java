package com.terra.framework.nova.llm.model.dify;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.AbstractVendorAdapter;
import com.terra.framework.nova.llm.model.AuthProvider;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.ModelRequest;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.llm.model.TokenUsage;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * Dify模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class DifyAdapter extends AbstractVendorAdapter {

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
    protected void customizeRequest(JSONObject vendorRequest, ModelRequest originalRequest) {
        // Dify特有的请求处理
        
        // 设置inputs和query
        vendorRequest.put("inputs", new JSONObject()); // 默认空的inputs
        
        // 根据请求类型处理
        if (originalRequest.getMessages() != null && !originalRequest.getMessages().isEmpty()) {
            vendorRequest.put("query", null); // 不使用query方式
        } else if (originalRequest.getPrompt() != null && !originalRequest.getPrompt().isEmpty()) {
            // 对于提示词，使用query模式
            vendorRequest.put("query", originalRequest.getPrompt());
            // 不设置消息
            vendorRequest.put("messages", new JSONArray());
        }

        // 设置对话模式（用户自定义选项）
        Object conversationId = originalRequest.getParameters().get("conversation_id");
        if (conversationId != null) {
            vendorRequest.put("conversation_id", conversationId.toString());
        }

        Object user = originalRequest.getParameters().get("user");
        if (user != null) {
            vendorRequest.put("user", user.toString());
        }
    }

    @Override
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        // 处理Dify特有的消息格式
        switch (originalMessage.getRole()) {
            case SYSTEM:
                // Dify没有system角色，但可以作为user的特殊消息
                vendorMessage.put("role", "user");
                vendorMessage.put("content", "[系统提示] " + originalMessage.getContent());
                break;
            case FUNCTION:
            case TOOL:
                // Dify可能不直接支持function和tool角色，作为assistant消息处理
                vendorMessage.put("role", "assistant");
                vendorMessage.put("content", "工具/函数调用结果: " + originalMessage.getContent());
                break;
        }
    }

    @Override
    protected void extractContent(JSONObject choice, ModelResponse modelResponse) {
        // Dify不使用标准的choices格式，这个方法不会被调用，但仍需实现
        super.extractContent(choice, modelResponse);
    }

    @Override
    protected void customizeResponse(ModelResponse modelResponse, JSONObject jsonResponse) {
        // 解析Dify特有的响应格式
        if (jsonResponse.containsKey("answer")) {
            String content = jsonResponse.getString("answer");
            modelResponse.setContent(content);
        } else if (jsonResponse.containsKey("event") && "message".equals(jsonResponse.getString("event"))) {
            // 解析流式响应
            if (jsonResponse.containsKey("answer")) {
                modelResponse.setContent(jsonResponse.getString("answer"));
            }
        }

        // 设置响应ID（Dify特有字段）
        if (jsonResponse.containsKey("conversation_id")) {
            modelResponse.setResponseId(jsonResponse.getString("conversation_id"));
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

    @Override
    protected ErrorType mapErrorType(String errorType) {
        if (errorType == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        switch (errorType) {
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

    @Override
    protected String getVendorName() {
        return "Dify";
    }

    @Override
    protected String getDefaultModelName() {
        return "dify-app";
    }
}
