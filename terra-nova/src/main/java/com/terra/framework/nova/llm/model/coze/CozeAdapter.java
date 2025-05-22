package com.terra.framework.nova.llm.model.coze;

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
import com.terra.framework.nova.llm.model.ToolCall;
import lombok.extern.slf4j.Slf4j;

import java.util.List;

/**
 * Coze模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class CozeAdapter extends AbstractVendorAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    public CozeAdapter(CozeRequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }
    
    @Override
    protected void customizeRequest(JSONObject vendorRequest, ModelRequest originalRequest) {
        super.customizeRequest(vendorRequest, originalRequest);
        
        if (originalRequest.getMessages() != null && !originalRequest.getMessages().isEmpty()) {
            log.debug("使用消息模式，确保Coze请求格式正确");
            
            // 处理工具配置 - Coze使用与OpenAI类似的工具格式
            if (originalRequest.getTools() != null && !originalRequest.getTools().isEmpty()) {
                vendorRequest.put("tools", JSON.toJSON(originalRequest.getTools()));
                log.debug("设置Coze工具定义: {}", originalRequest.getTools());
                
                // 设置工具选择策略
                if (originalRequest.getToolChoice() != null) {
                    vendorRequest.put("tool_choice", originalRequest.getToolChoice());
                    log.debug("设置Coze工具选择策略: {}", originalRequest.getToolChoice());
                } else {
                    vendorRequest.put("tool_choice", "auto");
                    log.debug("设置Coze默认工具选择策略: auto");
                }
            }
        }
    }
    
    @Override
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        super.customizeMessage(vendorMessage, originalMessage);
        
        // 处理工具相关的消息
        if (MessageRole.TOOL.equals(originalMessage.getRole()) || 
            MessageRole.FUNCTION.equals(originalMessage.getRole())) {
            
            // Coze可能使用与OpenAI类似的格式，确保name和tool_call_id正确设置
            if (originalMessage.getName() != null) {
                vendorMessage.put("name", originalMessage.getName());
            }
            
            if (originalMessage.getToolCallId() != null) {
                vendorMessage.put("tool_call_id", originalMessage.getToolCallId());
                log.debug("设置Coze工具调用ID: {}", originalMessage.getToolCallId());
            }
        }
        
        // 处理带工具调用的助手消息
        if (MessageRole.ASSISTANT.equals(originalMessage.getRole()) && 
            originalMessage.getToolCalls() != null && !originalMessage.getToolCalls().isEmpty()) {
            
            log.debug("Coze处理助手工具调用消息: {}", originalMessage.getToolCalls());
            vendorMessage.put("tool_calls", JSON.toJSON(originalMessage.getToolCalls()));
        }
    }
    
    @Override
    protected void extractContent(JSONObject choice, ModelResponse modelResponse) {
        super.extractContent(choice, modelResponse);
        
        // 特殊处理Coze的工具调用响应
        if (choice.containsKey("message") && choice.getJSONObject("message").containsKey("tool_calls")) {
            JSONArray toolCallsArray = choice.getJSONObject("message").getJSONArray("tool_calls");
            if (toolCallsArray != null && !toolCallsArray.isEmpty()) {
                try {
                    List<ToolCall> toolCalls = JSON.parseArray(toolCallsArray.toJSONString(), ToolCall.class);
                    modelResponse.setToolCalls(toolCalls);
                    log.debug("解析Coze工具调用响应: {}", toolCalls);
                } catch (Exception e) {
                    log.error("解析Coze工具调用失败: {}", e.getMessage(), e);
                }
            }
        }
    }
    
    @Override
    protected void customizeResponse(ModelResponse modelResponse, JSONObject jsonResponse) {
        super.customizeResponse(modelResponse, jsonResponse);
        
        // 确保处理可能直接包含在响应中的工具调用
        if (jsonResponse.containsKey("choices") && 
            jsonResponse.getJSONArray("choices").size() > 0 &&
            jsonResponse.getJSONArray("choices").getJSONObject(0).containsKey("message") &&
            jsonResponse.getJSONArray("choices").getJSONObject(0).getJSONObject("message").containsKey("tool_calls")) {
            
            JSONArray toolCallsArray = jsonResponse.getJSONArray("choices")
                .getJSONObject(0)
                .getJSONObject("message")
                .getJSONArray("tool_calls");
                
            modelResponse.setToolCalls(JSON.parseArray(toolCallsArray.toJSONString(), ToolCall.class));
            log.debug("从完整响应中解析Coze工具调用: {}", toolCallsArray);
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
                    String errorCode = error.getString("code");

                    ErrorType modelErrorType = mapErrorType(errorType);
                    return new ModelException(
                            "Coze API错误: " + errorMessage,
                            vendorException,
                            modelErrorType,
                            errorCode
                    );
                }
            }
        } catch (Exception e) {
            // 如果解析失败，使用默认处理
            log.debug("解析Coze错误失败，使用默认错误处理", e);
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
            case "model_not_available":
                return ErrorType.MODEL_UNAVAILABLE_ERROR;
            case "content_filtered":
                return ErrorType.CONTENT_FILTER_ERROR;
            default:
                return ErrorType.UNKNOWN_ERROR;
        }
    }

    @Override
    protected String getVendorName() {
        return "Coze";
    }

    @Override
    protected String getDefaultModelName() {
        return "gpt-3.5-turbo";
    }
} 