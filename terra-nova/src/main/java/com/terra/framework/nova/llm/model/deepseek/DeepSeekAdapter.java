package com.terra.framework.nova.llm.model.deepseek;

import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.model.AbstractVendorAdapter;
import com.terra.framework.nova.llm.model.AuthProvider;
import lombok.extern.slf4j.Slf4j;

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
}
