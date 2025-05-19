package com.terra.framework.nova.llm.model.tongyi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.AbstractVendorAdapter;
import com.terra.framework.nova.llm.model.AuthProvider;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.MessageRole;
import com.terra.framework.nova.llm.model.ModelRequest;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.llm.model.TokenUsage;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 通义千问模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class TongyiAdapter extends AbstractVendorAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    public TongyiAdapter(TongyiRequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }

    @Override
    public ModelException handleException(Exception vendorException) {
        try {
            if (vendorException.getMessage() != null && vendorException.getMessage().contains("code")) {
                JSONObject errorJson = JSON.parseObject(vendorException.getMessage());
                if (errorJson.containsKey("code")) {
                    String errorCode = errorJson.getString("code");
                    String errorMessage = errorJson.getString("message");

                    ErrorType modelErrorType = mapErrorType(errorCode);
                    return new ModelException(
                            "通义千问API错误: " + errorMessage,
                            vendorException,
                            modelErrorType,
                            errorCode
                    );
                }
            }
        } catch (Exception e) {
            // 如果解析失败，使用默认处理
            log.debug("解析通义千问错误失败，使用默认错误处理", e);
        }

        return super.handleException(vendorException);
    }

    @Override
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        // 处理通义千问特殊的消息角色转换
        if (originalMessage.getRole() == MessageRole.TOOL) {
            // 将tool角色映射到function角色
            vendorMessage.put("role", "function");
        }
    }

    @Override
    protected void customizeResponse(ModelResponse modelResponse, JSONObject jsonResponse) {
        // 处理通义千问特有的响应格式
        if (jsonResponse.containsKey("output")) {
            JSONObject output = jsonResponse.getJSONObject("output");

            // 从输出中获取内容
            if (output.containsKey("choices")) {
                JSONArray choices = output.getJSONArray("choices");
                if (!choices.isEmpty()) {
                    JSONObject firstChoice = choices.getJSONObject(0);

                    // 提取内容
                    if (firstChoice.containsKey("message")) {
                        JSONObject message = firstChoice.getJSONObject("message");
                        modelResponse.setContent(message.getString("content"));
                    } else if (firstChoice.containsKey("delta") &&
                               firstChoice.getJSONObject("delta").containsKey("content")) {
                        // 处理流式响应
                        modelResponse.setContent(firstChoice.getJSONObject("delta").getString("content"));
                    }
                }
            }
        }

        // 设置响应ID（通义千问特有的字段名）
        if (jsonResponse.containsKey("request_id")) {
            modelResponse.setResponseId(jsonResponse.getString("request_id"));
        }
    }

    @Override
    protected ErrorType mapErrorType(String errorCode) {
        if (errorCode == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        // 通义千问错误码映射
        switch (errorCode) {
            case "InvalidApiKey":
            case "Unauthorized":
                return ErrorType.AUTHENTICATION_ERROR;
            case "RequestRateLimit":
            case "QuotaExceeded":
                return ErrorType.RATE_LIMIT_ERROR;
            case "ContextLengthExceeded":
                return ErrorType.CONTEXT_LENGTH_ERROR;
            case "InvalidParameter":
            case "BadRequest":
                return ErrorType.INVALID_REQUEST_ERROR;
            case "InternalServerError":
            case "ServiceUnavailable":
                return ErrorType.SERVER_ERROR;
            case "ModelNotFound":
            case "ModelNotReady":
                return ErrorType.MODEL_UNAVAILABLE_ERROR;
            case "ContentFiltered":
                return ErrorType.CONTENT_FILTER_ERROR;
            default:
                return ErrorType.UNKNOWN_ERROR;
        }
    }

    @Override
    protected String getVendorName() {
        return "通义千问";
    }

    @Override
    protected String getDefaultModelName() {
        return "qwen-turbo";
    }
}
