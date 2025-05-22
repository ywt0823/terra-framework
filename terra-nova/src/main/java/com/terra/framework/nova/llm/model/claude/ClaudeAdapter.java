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
import com.terra.framework.nova.llm.model.ModelRequest;
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
    protected void customizeRequest(JSONObject vendorRequest, ModelRequest originalRequest) {
        super.customizeRequest(vendorRequest, originalRequest);
        
        if (originalRequest.getMessages() != null && !originalRequest.getMessages().isEmpty()) {
            log.debug("使用消息模式，确保Claude请求格式正确");
            
            // Claude模型的工具调用请求格式有所不同
            if (originalRequest.getTools() != null && !originalRequest.getTools().isEmpty()) {
                // Claude使用tools数组
                vendorRequest.put("tools", JSON.toJSON(originalRequest.getTools()));
                log.debug("设置Claude工具定义: {}", originalRequest.getTools());
                
                // 默认使用"auto"工具选择模式
                if (originalRequest.getToolChoice() != null) {
                    vendorRequest.put("tool_choice", originalRequest.getToolChoice());
                } else {
                    vendorRequest.put("tool_choice", "auto");
                }
                log.debug("设置Claude工具选择策略: {}", 
                    originalRequest.getToolChoice() != null ? originalRequest.getToolChoice() : "auto");
            }
        }
    }

    @Override
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        super.customizeMessage(vendorMessage, originalMessage);
        
        // Claude不支持function和tool角色，需要转换
        if (originalMessage.getRole() == MessageRole.FUNCTION || originalMessage.getRole() == MessageRole.TOOL) {
            vendorMessage.put("role", "user");
            // 如果有tool_call_id，确保添加
            if (originalMessage.getToolCallId() != null) {
                vendorMessage.put("tool_call_id", originalMessage.getToolCallId());
                log.debug("设置Claude工具调用ID: {}", originalMessage.getToolCallId());
            }
        }
        
        // 处理带工具调用的助手消息
        if (MessageRole.ASSISTANT.equals(originalMessage.getRole()) && 
            originalMessage.getToolCalls() != null && !originalMessage.getToolCalls().isEmpty()) {
            log.debug("Claude处理助手工具调用消息: {}", originalMessage.getToolCalls());
            vendorMessage.put("tool_calls", JSON.toJSON(originalMessage.getToolCalls()));
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
        
        // 处理工具调用响应
        if (choice.containsKey("message") && choice.getJSONObject("message").containsKey("tool_calls")) {
            JSONArray toolCallsArray = choice.getJSONObject("message").getJSONArray("tool_calls");
            if (toolCallsArray != null && !toolCallsArray.isEmpty()) {
                modelResponse.setToolCalls(JSON.parseArray(toolCallsArray.toJSONString(), 
                    com.terra.framework.nova.llm.model.ToolCall.class));
                log.debug("解析Claude工具调用响应: {}", toolCallsArray);
            }
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
        
        // 检查是否有工具调用但未被解析
        if (jsonResponse.containsKey("choices") && 
            jsonResponse.getJSONArray("choices").size() > 0 &&
            jsonResponse.getJSONArray("choices").getJSONObject(0).containsKey("message") &&
            jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").containsKey("tool_calls")) {
            
            JSONArray toolCallsArray = jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getJSONArray("tool_calls");
                
            modelResponse.setToolCalls(JSON.parseArray(toolCallsArray.toJSONString(), 
                com.terra.framework.nova.llm.model.ToolCall.class));
            log.debug("从完整响应中解析Claude工具调用: {}", toolCallsArray);
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
