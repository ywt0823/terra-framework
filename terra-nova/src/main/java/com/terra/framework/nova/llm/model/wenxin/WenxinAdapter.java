package com.terra.framework.nova.llm.model.wenxin;

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
 * 文心一言模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class WenxinAdapter extends AbstractVendorAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    public WenxinAdapter(WenxinRequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }

    @Override
    protected void customizeRequest(JSONObject vendorRequest, ModelRequest originalRequest) {
        super.customizeRequest(vendorRequest, originalRequest);
        
        if (originalRequest.getMessages() != null && !originalRequest.getMessages().isEmpty()) {
            log.debug("使用消息模式，确保文心一言请求格式正确");
            
            // 文心一言的函数调用格式与其他模型不同
            if (originalRequest.getTools() != null && !originalRequest.getTools().isEmpty()) {
                // 文心一言使用functions数组而不是tools
                JSONArray functionsArray = new JSONArray();
                for (Object tool : originalRequest.getTools()) {
                    if (tool instanceof Map) {
                        Map<String, Object> toolMap = (Map<String, Object>)tool;
                        // 只处理function类型的工具
                        if ("function".equals(toolMap.get("type"))) {
                            Map<String, Object> functionDetails = (Map<String, Object>)toolMap.get("function");
                            functionsArray.add(functionDetails);
                        }
                    }
                }
                
                if (!functionsArray.isEmpty()) {
                    vendorRequest.put("functions", functionsArray);
                    log.debug("设置文心一言函数定义: {}", functionsArray);
                    
                    // 设置函数调用策略
                    if (originalRequest.getToolChoice() != null) {
                        if ("auto".equals(originalRequest.getToolChoice())) {
                            vendorRequest.put("function_call", "auto");
                        } else if (originalRequest.getToolChoice() instanceof Map) {
                            // 文心一言特定函数选择格式
                            Map<String, Object> choice = (Map<String, Object>)originalRequest.getToolChoice();
                            if (choice.containsKey("function")) {
                                Map<String, Object> functionChoice = (Map<String, Object>)choice.get("function");
                                String name = (String)functionChoice.get("name");
                                if (name != null) {
                                    JSONObject functionCall = new JSONObject();
                                    functionCall.put("name", name);
                                    vendorRequest.put("function_call", functionCall);
                                }
                            }
                        }
                    } else {
                        vendorRequest.put("function_call", "auto");
                    }
                    log.debug("设置文心一言函数调用策略: {}", vendorRequest.get("function_call"));
                }
            }
        }
    }

    @Override
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        super.customizeMessage(vendorMessage, originalMessage);
        
        // 文心一言特殊处理角色
        switch (originalMessage.getRole()) {
            case SYSTEM:
                // 文心一言不支持system角色，将其转换为user角色并添加标记
                vendorMessage.put("role", "user");
                vendorMessage.put("content", "[系统指令]" + originalMessage.getContent());
                break;
            case FUNCTION:
            case TOOL:
                // 文心一言使用不同的格式表示函数响应
                vendorMessage.put("role", "assistant");
                // 如果有函数名称，添加函数响应格式
                if (originalMessage.getName() != null) {
                    JSONObject functionResponse = new JSONObject();
                    functionResponse.put("name", originalMessage.getName());
                    functionResponse.put("content", originalMessage.getContent());
                    vendorMessage.put("function_call", functionResponse);
                    // 清空内容，避免重复
                    vendorMessage.put("content", null);
                } else {
                    vendorMessage.put("content", "工具调用结果: " + originalMessage.getContent());
                }
                break;
            case ASSISTANT:
                // 处理带函数调用的助手消息
                if (originalMessage.getToolCalls() != null && !originalMessage.getToolCalls().isEmpty()) {
                    // 文心一言只支持单个函数调用
                    ToolCall toolCall = originalMessage.getToolCalls().get(0);
                    if (toolCall.getFunction() != null) {
                        JSONObject functionCall = new JSONObject();
                        functionCall.put("name", toolCall.getFunction().getName());
                        functionCall.put("arguments", toolCall.getFunction().getArguments());
                        vendorMessage.put("function_call", functionCall);
                        // 如果没有内容，确保content字段为空
                        if (originalMessage.getContent() == null) {
                            vendorMessage.put("content", null);
                        }
                    }
                }
                break;
        }
    }
    
    @Override
    protected void extractContent(JSONObject choice, ModelResponse modelResponse) {
        super.extractContent(choice, modelResponse);
        
        // 文心一言的函数调用响应格式
        if (choice.containsKey("function_call")) {
            JSONObject functionCall = choice.getJSONObject("function_call");
            String name = functionCall.getString("name");
            String arguments = functionCall.getString("arguments");
            log.debug("解析文心一言函数调用: name={}, arguments={}", name, arguments);
            
            FunctionCallInfo functionCallInfo = FunctionCallInfo.builder()
                .name(name)
                .arguments(arguments)
                .build();
                
            // 生成一个唯一ID
            String id = "tool-" + System.currentTimeMillis();
            
            ToolCall toolCall = ToolCall.builder()
                .id(id)
                .type("function")
                .function(functionCallInfo)
                .build();
                
            List<ToolCall> toolCalls = new ArrayList<>();
            toolCalls.add(toolCall);
            modelResponse.setToolCalls(toolCalls);
            
            // 当有函数调用时，通常内容为空
            modelResponse.setContent(null);
        }
    }

    @Override
    protected ErrorType mapErrorType(String errorCode) {
        if (errorCode == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        // 文心一言的错误码参考
        switch (errorCode) {
            case "2": // 请求参数错误
                return ErrorType.INVALID_REQUEST_ERROR;
            case "4":  // 授权错误
            case "6":  // 身份认证失败
            case "14": // Access Token过期
            case "15": // Access Token不存在
            case "17": // Open API 请求数达到上限
            case "18": // Open API 调用频率达到上限
                return ErrorType.AUTHENTICATION_ERROR;
            case "19": // 模型调用超限
            case "336100": // QPS 限流
                return ErrorType.RATE_LIMIT_ERROR;
            case "336101": // 文本安全
                return ErrorType.CONTENT_FILTER_ERROR;
            case "336102": // prompt 尺寸超限
                return ErrorType.CONTEXT_LENGTH_ERROR;
            case "336103": // 模型服务化预估内部错误
            case "336104": // 模型服务化预估内部错误
                return ErrorType.SERVER_ERROR;
            default:
                return ErrorType.UNKNOWN_ERROR;
        }
    }

    @Override
    protected String getVendorName() {
        return "文心一言";
    }

    @Override
    protected String getDefaultModelName() {
        return "ernie-bot";
    }
}
