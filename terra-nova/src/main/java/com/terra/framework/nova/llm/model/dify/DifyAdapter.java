package com.terra.framework.nova.llm.model.dify;

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
import com.terra.framework.nova.llm.model.ToolCall;
import com.terra.framework.nova.llm.model.FunctionCallInfo;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
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
        super.customizeRequest(vendorRequest, originalRequest);
        
        // Dify特有的请求处理
        
        // 设置inputs和query
        vendorRequest.put("inputs", new JSONObject()); // 默认空的inputs
        
        // 根据请求类型处理
        if (originalRequest.getMessages() != null && !originalRequest.getMessages().isEmpty()) {
            vendorRequest.put("query", null); // 不使用query方式
            
            // 处理工具调用 - Dify支持通过tools参数传递函数定义
            if (originalRequest.getTools() != null && !originalRequest.getTools().isEmpty()) {
                // Dify使用tools数组 - 格式可能与标准OpenAI格式略有不同
                JSONArray toolsArray = new JSONArray();
                
                for (Object tool : originalRequest.getTools()) {
                    if (tool instanceof Map) {
                        Map<String, Object> toolMap = (Map<String, Object>) tool;
                        
                        // 处理函数类型的工具
                        if ("function".equals(toolMap.get("type")) && toolMap.containsKey("function")) {
                            toolsArray.add(toolMap);
                        }
                    }
                }
                
                if (!toolsArray.isEmpty()) {
                    // 在inputs中添加工具定义
                    JSONObject inputs = vendorRequest.getJSONObject("inputs");
                    inputs.put("tools", toolsArray);
                    log.debug("设置Dify工具定义: {}", toolsArray);
                    
                    // 设置工具选择策略
                    if (originalRequest.getToolChoice() != null) {
                        inputs.put("tool_choice", originalRequest.getToolChoice());
                        log.debug("设置Dify工具选择策略: {}", originalRequest.getToolChoice());
                    } else {
                        inputs.put("tool_choice", "auto");
                        log.debug("设置Dify默认工具选择策略: auto");
                    }
                }
            }
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
        super.customizeMessage(vendorMessage, originalMessage);
        
        // 处理Dify特有的消息格式
        switch (originalMessage.getRole()) {
            case SYSTEM:
                // Dify没有system角色，但可以作为user的特殊消息
                vendorMessage.put("role", "user");
                vendorMessage.put("content", "[系统提示] " + originalMessage.getContent());
                break;
            case FUNCTION:
            case TOOL:
                // Dify处理工具响应
                vendorMessage.put("role", "assistant");
                
                // 添加工具名称和ID（如果有）
                if (originalMessage.getName() != null) {
                    vendorMessage.put("name", originalMessage.getName());
                }
                
                if (originalMessage.getToolCallId() != null) {
                    vendorMessage.put("tool_call_id", originalMessage.getToolCallId());
                    log.debug("设置Dify工具调用ID: {}", originalMessage.getToolCallId());
                }
                
                // 内容格式特殊处理
                vendorMessage.put("content", "工具/函数调用结果: " + originalMessage.getContent());
                break;
            case ASSISTANT:
                // 处理助手消息中的工具调用
                if (originalMessage.getToolCalls() != null && !originalMessage.getToolCalls().isEmpty()) {
                    log.debug("Dify处理助手工具调用消息: {}", originalMessage.getToolCalls());
                    vendorMessage.put("tool_calls", JSON.toJSON(originalMessage.getToolCalls()));
                }
                break;
        }
    }

    @Override
    protected void extractContent(JSONObject choice, ModelResponse modelResponse) {
        // 使用父类的方法提取基本内容
        super.extractTextContent(choice, modelResponse);
        
        // Dify特有的工具调用格式处理
        if (choice.containsKey("tool_calls")) {
            JSONArray toolCallsArray = choice.getJSONArray("tool_calls");
            if (toolCallsArray != null && !toolCallsArray.isEmpty()) {
                List<ToolCall> toolCalls = new ArrayList<>();
                
                for (int i = 0; i < toolCallsArray.size(); i++) {
                    JSONObject callObject = toolCallsArray.getJSONObject(i);
                    String id = callObject.getString("id");
                    String type = callObject.getString("type");
                    
                    if ("function".equals(type) && callObject.containsKey("function")) {
                        JSONObject functionObject = callObject.getJSONObject("function");
                        String name = functionObject.getString("name");
                        String arguments = functionObject.getString("arguments");
                        
                        FunctionCallInfo functionCall = FunctionCallInfo.builder()
                            .name(name)
                            .arguments(arguments)
                            .build();
                            
                        ToolCall toolCall = ToolCall.builder()
                            .id(id)
                            .type(type)
                            .function(functionCall)
                            .build();
                            
                        toolCalls.add(toolCall);
                    }
                }
                
                if (!toolCalls.isEmpty()) {
                    modelResponse.setToolCalls(toolCalls);
                    log.debug("{}解析特殊格式工具调用: {}", getVendorName(), toolCalls);
                }
            }
        } else {
            // 对于标准格式的工具调用，使用父类方法
            super.extractToolCalls(choice, modelResponse);
        }
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
        
        // 处理工具调用 - Dify可能以特殊格式返回工具调用
        if (jsonResponse.containsKey("tool_calls")) {
            JSONArray toolCallsArray = jsonResponse.getJSONArray("tool_calls");
            if (toolCallsArray != null && !toolCallsArray.isEmpty()) {
                try {
                    List<ToolCall> toolCalls = JSON.parseArray(toolCallsArray.toJSONString(), ToolCall.class);
                    modelResponse.setToolCalls(toolCalls);
                    log.debug("解析Dify工具调用响应: {}", toolCalls);
                } catch (Exception e) {
                    log.error("解析Dify工具调用失败: {}", e.getMessage());
                }
            }
        }
        
        // 如果响应中包含工具调用信息，但格式特殊，尝试解析
        if (jsonResponse.containsKey("tool_call") || 
            (jsonResponse.containsKey("extra") && jsonResponse.getJSONObject("extra").containsKey("tool_call"))) {
            
            JSONObject toolCallObj = jsonResponse.containsKey("tool_call") ? 
                jsonResponse.getJSONObject("tool_call") : 
                jsonResponse.getJSONObject("extra").getJSONObject("tool_call");
                
            // 确保工具调用包含必要字段
            if (toolCallObj.containsKey("name") && toolCallObj.containsKey("arguments")) {
                // 为工具调用生成唯一ID
                String id = "tool-" + System.currentTimeMillis();
                String name = toolCallObj.getString("name");
                String arguments = toolCallObj.getString("arguments");
                
                FunctionCallInfo functionCall = FunctionCallInfo.builder()
                    .name(name)
                    .arguments(arguments)
                    .build();
                    
                ToolCall toolCall = ToolCall.builder()
                    .id(id)
                    .type("function")
                    .function(functionCall)
                    .build();
                
                List<ToolCall> toolCalls = new ArrayList<>();
                toolCalls.add(toolCall);
                modelResponse.setToolCalls(toolCalls);
                
                log.debug("从特殊格式中解析Dify工具调用: {}", toolCall);
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
