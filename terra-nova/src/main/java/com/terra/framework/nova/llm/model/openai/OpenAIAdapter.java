package com.terra.framework.nova.llm.model.openai;

import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.model.AbstractVendorAdapter;
import com.terra.framework.nova.llm.model.AuthProvider;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class OpenAIAdapter extends AbstractVendorAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    public OpenAIAdapter(OpenAIRequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }

    @Override
    protected void processPrompt(String prompt, JSONObject vendorRequest, String model) {
        // 对于OpenAI，text-开头的模型使用不同的参数名
        if (model.startsWith("text-")) {
            vendorRequest.put("prompt", prompt);
        } else {
            // 对于聊天模型，使用默认的消息格式
            super.processPrompt(prompt, vendorRequest, model);
        }
    }

    @Override
    protected ErrorType mapErrorType(String errorType) {
        if (errorType == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        switch (errorType) {
            case "authentication_error":
                return ErrorType.AUTHENTICATION_ERROR;
            case "rate_limit_exceeded":
                return ErrorType.RATE_LIMIT_ERROR;
            case "context_length_exceeded":
                return ErrorType.CONTEXT_LENGTH_ERROR;
            case "invalid_request_error":
                return ErrorType.INVALID_REQUEST_ERROR;
            case "server_error":
                return ErrorType.SERVER_ERROR;
            case "model_overloaded":
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
        return "OpenAI";
    }

    @Override
    protected String getDefaultModelName() {
        return "gpt-3.5-turbo";
    }
}
