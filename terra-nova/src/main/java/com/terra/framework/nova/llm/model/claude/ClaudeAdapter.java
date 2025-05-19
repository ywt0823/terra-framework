package com.terra.framework.nova.llm.model.claude;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.AbstractVendorAdapter;
import com.terra.framework.nova.llm.model.AuthProvider;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.MessageRole;
import com.terra.framework.nova.llm.model.ModelResponse;
import lombok.extern.slf4j.Slf4j;

/**
 * Claude模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class ClaudeAdapter extends AbstractVendorAdapter {

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
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        // Claude不支持function和tool角色，需要转换
        if (originalMessage.getRole() == MessageRole.FUNCTION || originalMessage.getRole() == MessageRole.TOOL) {
            vendorMessage.put("role", "user");
        }
    }

    @Override
    protected void extractContent(JSONObject choice, ModelResponse modelResponse) {
        // Claude的响应格式可能有所不同
        if (choice.containsKey("content")) {
            modelResponse.setContent(choice.getString("content"));
        } else if (choice.containsKey("delta") && choice.getJSONObject("delta").containsKey("text")) {
            // 处理流式响应
            modelResponse.setContent(choice.getJSONObject("delta").getString("text"));
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

    @Override
    protected void customizeResponse(ModelResponse modelResponse, JSONObject jsonResponse) {
        // Claude可能有一些特殊的响应格式处理
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
        }
    }

    @Override
    protected ErrorType mapErrorType(String errorType) {
        if (errorType == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        switch (errorType) {
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

    @Override
    protected String getVendorName() {
        return "Claude";
    }

    @Override
    protected String getDefaultModelName() {
        return "claude-3-opus-20240229";
    }
}
