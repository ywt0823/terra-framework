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
import com.terra.framework.nova.llm.model.ToolCall;
import com.terra.framework.nova.llm.model.FunctionCallInfo;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
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
    protected void customizeRequest(JSONObject vendorRequest, ModelRequest originalRequest) {
        super.customizeRequest(vendorRequest, originalRequest);
        
        if (originalRequest.getMessages() != null && !originalRequest.getMessages().isEmpty()) {
            log.debug("使用消息模式，确保通义千问请求格式正确");
            
            // 处理工具/函数调用相关配置
            if (originalRequest.getTools() != null && !originalRequest.getTools().isEmpty()) {
                // 通义千问使用tools数组，但格式可能有所不同
                JSONArray toolsArray = new JSONArray();
                
                // 转换工具定义
                for (Object tool : originalRequest.getTools()) {
                    if (tool instanceof Map) {
                        Map<String, Object> toolMap = (Map<String, Object>) tool;
                        // 处理function类型的工具
                        if ("function".equals(toolMap.get("type")) && toolMap.containsKey("function")) {
                            toolsArray.add(toolMap);
                        }
                    }
                }
                
                if (!toolsArray.isEmpty()) {
                    vendorRequest.put("tools", toolsArray);
                    log.debug("设置通义千问工具定义: {}", toolsArray);
                    
                    // 设置工具调用策略
                    if (originalRequest.getToolChoice() != null) {
                        vendorRequest.put("tool_choice", originalRequest.getToolChoice());
                        log.debug("设置通义千问工具选择策略: {}", originalRequest.getToolChoice());
                    } else {
                        vendorRequest.put("tool_choice", "auto");
                        log.debug("设置通义千问默认工具选择策略: auto");
                    }
                }
            }
        }
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
        super.customizeMessage(vendorMessage, originalMessage);
        
        // 处理通义千问特殊的消息角色转换
        if (originalMessage.getRole() == MessageRole.TOOL) {
            // 将tool角色映射到function角色
            vendorMessage.put("role", "function");
            
            // 确保设置了工具名称
            if (originalMessage.getName() != null) {
                vendorMessage.put("name", originalMessage.getName());
            }
            
            log.debug("将TOOL角色转换为function角色: {}", originalMessage.getName());
        }
        
        // 处理带工具调用的助手消息
        if (MessageRole.ASSISTANT.equals(originalMessage.getRole()) && 
            originalMessage.getToolCalls() != null && !originalMessage.getToolCalls().isEmpty()) {
            
            log.debug("通义千问处理助手工具调用消息: {}", originalMessage.getToolCalls());
            // 确保工具调用格式与通义千问预期一致
            // 通常格式应为 tool_calls 数组
            vendorMessage.put("tool_calls", JSON.toJSON(originalMessage.getToolCalls()));
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
                        
                        // 处理工具调用
                        if (message.containsKey("tool_calls")) {
                            JSONArray toolCallsArray = message.getJSONArray("tool_calls");
                            if (toolCallsArray != null && !toolCallsArray.isEmpty()) {
                                try {
                                    List<ToolCall> toolCalls = JSON.parseArray(toolCallsArray.toJSONString(), 
                                        ToolCall.class);
                                    modelResponse.setToolCalls(toolCalls);
                                    log.debug("解析通义千问工具调用: {}", toolCalls);
                                } catch (Exception e) {
                                    log.error("解析通义千问工具调用失败: {}", e.getMessage());
                                }
                            }
                        }
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
    protected void extractContent(JSONObject choice, ModelResponse modelResponse) {
        super.extractContent(choice, modelResponse);
        
        // 通义千问可能有特殊的工具调用格式
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
                }
            }
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
