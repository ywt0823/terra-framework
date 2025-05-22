package com.terra.framework.nova.llm.model.deepseek;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.model.AbstractVendorAdapter;
import com.terra.framework.nova.llm.model.AuthProvider;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.MessageRole;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * DeepSeek模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class DeepSeekAdapter extends AbstractVendorAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    public DeepSeekAdapter(DeepSeekRequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }

    @Override
    protected void processPrompt(String prompt, JSONObject vendorRequest, String model) {
        // 对于非chat模型，使用prompt字段
        vendorRequest.put("prompt", prompt);
    }

    @Override
    protected void customizeRequest(JSONObject vendorRequest, com.terra.framework.nova.llm.model.ModelRequest originalRequest) {
        super.customizeRequest(vendorRequest, originalRequest);

        // 确保在Chat模式下使用正确的格式
        if (originalRequest.getMessages() != null && !originalRequest.getMessages().isEmpty()) {
            log.debug("使用消息模式，确保DeepSeek请求格式正确");

            // 如果设置了工具但没有设置工具选择策略，默认使用auto
            if (vendorRequest.containsKey("tools") && !vendorRequest.containsKey("tool_choice")) {
                vendorRequest.put("tool_choice", "auto");
                log.debug("设置默认工具选择策略: auto");
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
                return ErrorType.AUTHENTICATION_ERROR;
            case "rate_limit_exceeded":
                return ErrorType.RATE_LIMIT_ERROR;
            case "context_length_exceeded":
                return ErrorType.CONTEXT_LENGTH_ERROR;
            case "invalid_request_error":
                return ErrorType.INVALID_REQUEST_ERROR;
            case "server_error":
                return ErrorType.SERVER_ERROR;
            case "model_not_found":
                return ErrorType.MODEL_UNAVAILABLE_ERROR;
            case "content_filter":
                return ErrorType.CONTENT_FILTER_ERROR;
            default:
                return ErrorType.UNKNOWN_ERROR;
        }
    }

    @Override
    protected String getVendorName() {
        return "DeepSeek";
    }

    @Override
    protected String getDefaultModelName() {
        return "deepseek-chat";
    }

    @Override
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        super.customizeMessage(vendorMessage, originalMessage);

        if (MessageRole.ASSISTANT.equals(originalMessage.getRole()) &&
            originalMessage.getToolCalls() != null && !originalMessage.getToolCalls().isEmpty()) {
            log.debug("DeepSeek处理助手工具调用消息: {}", originalMessage.getToolCalls());
            // 确保以DeepSeek期望的格式设置工具调用
            // 大多数情况下super.customizeMessage已经处理了，这里只是额外确认
        }
    }
}
