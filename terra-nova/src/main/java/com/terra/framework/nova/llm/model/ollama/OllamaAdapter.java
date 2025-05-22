package com.terra.framework.nova.llm.model.ollama;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.AbstractVendorAdapter;
import com.terra.framework.nova.llm.model.AuthProvider;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.ModelRequest;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.llm.model.TokenUsage;
import com.terra.framework.nova.llm.model.MessageRole;
import com.terra.framework.nova.llm.model.ToolCall;
import com.terra.framework.nova.llm.model.FunctionCallInfo;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import lombok.extern.slf4j.Slf4j;

/**
 * Ollama模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class OllamaAdapter extends AbstractVendorAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    public OllamaAdapter(OllamaRequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }

    @Override
    protected void processPrompt(String prompt, JSONObject vendorRequest, String model) {
        // Ollama使用prompt字段
        vendorRequest.put("prompt", prompt);
    }

    @Override
    protected void customizeRequest(JSONObject vendorRequest, ModelRequest originalRequest) {
        super.customizeRequest(vendorRequest, originalRequest);
        
        // 如果有消息并且不使用chat API，将消息转换为文本提示词
        if (originalRequest.getMessages() != null && !originalRequest.getMessages().isEmpty() 
            && !isChatCompletions(originalRequest.getParameters())) {
            // 替换掉之前设置的messages
            if (vendorRequest.containsKey("messages")) {
                vendorRequest.remove("messages");
            }
            // 使用generate接口，将消息转换为提示文本
            vendorRequest.put("prompt", convertMessagesToPrompt(originalRequest.getMessages()));
        }
        
        // 如果使用chat接口，并且有函数调用工具，需要特殊处理
        if (isChatCompletions(originalRequest.getParameters()) && 
            originalRequest.getTools() != null && !originalRequest.getTools().isEmpty()) {
            
            log.debug("Ollama使用chat接口并配置工具，进行函数调用支持处理");
            
            // 注意：Ollama的老版本可能不原生支持工具调用，但我们会将工具信息以特殊格式添加到系统提示中
            boolean hasSystemMessage = false;
            String toolsDescription = convertToolsToDescription(originalRequest.getTools());
            
            if (vendorRequest.containsKey("messages")) {
                JSONArray messages = vendorRequest.getJSONArray("messages");
                
                // 查找是否已有系统消息
                for (int i = 0; i < messages.size(); i++) {
                    JSONObject message = messages.getJSONObject(i);
                    if ("system".equals(message.getString("role"))) {
                        // 追加工具信息到现有系统消息
                        String currentContent = message.getString("content");
                        message.put("content", currentContent + "\n\n" + toolsDescription);
                        hasSystemMessage = true;
                        break;
                    }
                }
                
                // 如果没有系统消息，添加一个
                if (!hasSystemMessage) {
                    JSONObject systemMessage = new JSONObject();
                    systemMessage.put("role", "system");
                    systemMessage.put("content", toolsDescription);
                    
                    // 在消息列表开头添加系统消息
                    JSONArray newMessages = new JSONArray();
                    newMessages.add(systemMessage);
                    for (int i = 0; i < messages.size(); i++) {
                        newMessages.add(messages.getJSONObject(i));
                    }
                    
                    vendorRequest.put("messages", newMessages);
                }
                
                log.debug("已将工具定义添加到Ollama系统提示中");
            }
        }
    }
    
    /**
     * 将工具定义转换为文本描述，供Ollama使用
     *
     * @param tools 工具定义列表
     * @return 工具文本描述
     */
    @SuppressWarnings("unchecked")
    private String convertToolsToDescription(List<?> tools) {
        StringBuilder builder = new StringBuilder();
        builder.append("你可以使用以下工具来回答问题。当需要使用工具时，请使用以下JSON格式输出：\n\n");
        builder.append("```json\n{\"tool_calls\": [{\"type\": \"function\", \"function\": {\"name\": \"工具名称\", \"arguments\": {参数JSON}}}]}\n```\n\n");
        builder.append("可用工具列表：\n\n");
        
        for (Object tool : tools) {
            if (tool instanceof Map) {
                Map<String, Object> toolMap = (Map<String, Object>) tool;
                if ("function".equals(toolMap.get("type")) && toolMap.containsKey("function")) {
                    Map<String, Object> functionMap = (Map<String, Object>) toolMap.get("function");
                    String name = (String) functionMap.get("name");
                    String description = (String) functionMap.get("description");
                    
                    builder.append("- 工具名称: ").append(name).append("\n");
                    builder.append("  描述: ").append(description).append("\n");
                    
                    // 添加参数信息
                    if (functionMap.containsKey("parameters")) {
                        Map<String, Object> paramsMap = (Map<String, Object>) functionMap.get("parameters");
                        if (paramsMap.containsKey("properties")) {
                            Map<String, Object> properties = (Map<String, Object>) paramsMap.get("properties");
                            
                            builder.append("  参数:\n");
                            for (Map.Entry<String, Object> entry : properties.entrySet()) {
                                String paramName = entry.getKey();
                                Map<String, Object> paramDetails = (Map<String, Object>) entry.getValue();
                                
                                String paramType = "字符串";
                                if (paramDetails.containsKey("type")) {
                                    String type = (String) paramDetails.get("type");
                                    switch (type) {
                                        case "string": paramType = "字符串"; break;
                                        case "number": paramType = "数字"; break;
                                        case "boolean": paramType = "布尔值"; break;
                                        case "array": paramType = "数组"; break;
                                        case "object": paramType = "对象"; break;
                                        default: paramType = type;
                                    }
                                }
                                
                                String paramDesc = (String) paramDetails.getOrDefault("description", "");
                                
                                builder.append("    - ").append(paramName)
                                      .append(" (").append(paramType).append("): ")
                                      .append(paramDesc).append("\n");
                            }
                        }
                    }
                    
                    builder.append("\n");
                }
            }
        }
        
        return builder.toString();
    }

    /**
     * 判断是否使用chat/completions接口
     *
     * @param parameters 参数
     * @return 是否使用chat/completions接口
     */
    private boolean isChatCompletions(Map<String, Object> parameters) {
        Object useChatObj = parameters.get("use_chat_api");
        if (useChatObj != null) {
            if (useChatObj instanceof Boolean) {
                return (Boolean) useChatObj;
            }
            return Boolean.parseBoolean(useChatObj.toString());
        }
        // 默认情况下，如果未指定，则根据模型类型判断
        String model = getModelName(parameters);
        // 判断模型是否支持聊天接口
        return model.contains("chat") || model.contains("instruct");
    }

    /**
     * 将消息列表转换为文本提示词
     *
     * @param messages 消息列表
     * @return 文本提示词
     */
    private String convertMessagesToPrompt(List<Message> messages) {
        StringBuilder promptBuilder = new StringBuilder();

        for (Message message : messages) {
            switch (message.getRole()) {
                case SYSTEM:
                    promptBuilder.append("[系统]: ").append(message.getContent()).append("\n\n");
                    break;
                case USER:
                    promptBuilder.append("[用户]: ").append(message.getContent()).append("\n\n");
                    break;
                case ASSISTANT:
                    promptBuilder.append("[助手]: ").append(message.getContent()).append("\n\n");
                    break;
                case FUNCTION:
                case TOOL:
                    promptBuilder.append("[工具调用结果]: ").append(message.getContent()).append("\n\n");
                    break;
                default:
                    promptBuilder.append(message.getContent()).append("\n\n");
            }
        }

        // 添加最后的提示词
        promptBuilder.append("[助手]: ");

        return promptBuilder.toString();
    }

    @Override
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        super.customizeMessage(vendorMessage, originalMessage);
        
        // 特殊处理function或tool角色
        if (originalMessage.getRole() == MessageRole.FUNCTION 
            || originalMessage.getRole() == MessageRole.TOOL) {
            // Ollama可能不支持function或tool角色，但为了兼容性，我们将其转换为assistant
            vendorMessage.put("role", "assistant");
            vendorMessage.put("content", "[工具调用结果] " + originalMessage.getContent());
        }
        
        // 处理带工具调用的助手消息
        if (MessageRole.ASSISTANT.equals(originalMessage.getRole()) && 
            originalMessage.getToolCalls() != null && !originalMessage.getToolCalls().isEmpty()) {
            
            // 由于Ollama可能不直接支持工具调用，我们将其以特殊格式转换到内容中
            JSONObject toolCallsObject = new JSONObject();
            toolCallsObject.put("tool_calls", JSON.toJSON(originalMessage.getToolCalls()));
            
            // 将对象转为JSON字符串
            String toolCallsJson = JSON.toJSONString(toolCallsObject, true);
            
            // 构建内容
            String content = originalMessage.getContent();
            if (content == null) {
                content = "";
            }
            
            content += "\n\n```json\n" + toolCallsJson + "\n```";
            
            vendorMessage.put("content", content);
            log.debug("将Ollama工具调用转换为JSON格式: {}", toolCallsJson);
        }
    }

    @Override
    protected void extractContent(JSONObject choice, ModelResponse modelResponse) {
        // 首先使用标准方法提取内容
        super.extractContent(choice, modelResponse);
        
        // 尝试从内容中解析工具调用
        String content = modelResponse.getContent();
        if (content != null && !content.isEmpty()) {
            // 查找JSON代码块
            int jsonStart = content.indexOf("```json");
            int jsonEnd = content.lastIndexOf("```");
            
            if (jsonStart >= 0 && jsonEnd > jsonStart) {
                // 提取JSON部分
                String jsonStr = content.substring(jsonStart + 7, jsonEnd).trim();
                try {
                    JSONObject toolCallsObj = JSON.parseObject(jsonStr);
                    
                    // 如果包含tool_calls字段
                    if (toolCallsObj.containsKey("tool_calls")) {
                        JSONArray toolCallsArray = toolCallsObj.getJSONArray("tool_calls");
                        
                        if (toolCallsArray != null && !toolCallsArray.isEmpty()) {
                            List<ToolCall> toolCalls = new ArrayList<>();
                            
                            for (int i = 0; i < toolCallsArray.size(); i++) {
                                JSONObject callObject = toolCallsArray.getJSONObject(i);
                                
                                // 为每个调用生成唯一ID
                                String id = "tool-" + System.currentTimeMillis() + "-" + i;
                                String type = callObject.getString("type");
                                
                                if ("function".equals(type) && callObject.containsKey("function")) {
                                    JSONObject functionObject = callObject.getJSONObject("function");
                                    String name = functionObject.getString("name");
                                    
                                    // 获取参数 - 可能是字符串或对象
                                    String arguments;
                                    if (functionObject.get("arguments") instanceof String) {
                                        arguments = functionObject.getString("arguments");
                                    } else {
                                        arguments = functionObject.getJSONObject("arguments").toJSONString();
                                    }
                                    
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
                                // 设置工具调用
                                modelResponse.setToolCalls(toolCalls);
                                
                                // 更新内容，移除JSON部分
                                String cleanContent = content.substring(0, jsonStart).trim();
                                modelResponse.setContent(cleanContent);
                                
                                log.debug("从Ollama响应中提取出工具调用: {}", toolCalls);
                            }
                        }
                    }
                } catch (Exception e) {
                    log.error("解析Ollama工具调用JSON失败: {}", e.getMessage());
                }
            }
        }
        
        // Ollama使用不同的字段提取普通内容
        if (modelResponse.getContent() == null) {
            if (choice.containsKey("response")) {
                // generate接口的响应
                modelResponse.setContent(choice.getString("response"));
            } else if (choice.containsKey("message")) {
                // chat接口的响应
                JSONObject message = choice.getJSONObject("message");
                modelResponse.setContent(message.getString("content"));
            } else if (choice.containsKey("content")) {
                // 流式响应中的内容
                modelResponse.setContent(choice.getString("content"));
            }
        }
    }

    @Override
    protected void customizeResponse(ModelResponse modelResponse, JSONObject jsonResponse) {
        // 设置Token使用情况（Ollama特有字段）
        if (jsonResponse.containsKey("prompt_eval_count") && jsonResponse.containsKey("eval_count")) {
            TokenUsage tokenUsage = TokenUsage.of(
                    jsonResponse.getIntValue("prompt_eval_count"),
                    jsonResponse.getIntValue("eval_count")
            );
            modelResponse.setTokenUsage(tokenUsage);
        }
    }

    @Override
    protected ErrorType mapErrorType(String errorType) {
        if (errorType == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        errorType = errorType.toLowerCase();

        if (errorType.contains("auth") || errorType.contains("unauthorized") || errorType.contains("token")) {
            return ErrorType.AUTHENTICATION_ERROR;
        } else if (errorType.contains("rate") || errorType.contains("limit")) {
            return ErrorType.RATE_LIMIT_ERROR;
        } else if (errorType.contains("context") || errorType.contains("length") || errorType.contains("too large")) {
            return ErrorType.CONTEXT_LENGTH_ERROR;
        } else if (errorType.contains("invalid") || errorType.contains("parameter") || errorType.contains("format")) {
            return ErrorType.INVALID_REQUEST_ERROR;
        } else if (errorType.contains("server") || errorType.contains("internal")) {
            return ErrorType.SERVER_ERROR;
        } else if (errorType.contains("not found") || errorType.contains("model") || errorType.contains("unavailable")) {
            return ErrorType.MODEL_UNAVAILABLE_ERROR;
        } else if (errorType.contains("content") || errorType.contains("filter") || errorType.contains("moderation")) {
            return ErrorType.CONTENT_FILTER_ERROR;
        } else {
            return ErrorType.UNKNOWN_ERROR;
        }
    }

    @Override
    protected String getVendorName() {
        return "Ollama";
    }

    @Override
    protected String getDefaultModelName() {
        return "llama2";
    }
}
