package com.terra.framework.nova.llm.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.exception.ModelException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * 抽象供应商适配器实现，提供通用功能
 *
 * @author terra-nova
 */
@Slf4j
public abstract class AbstractVendorAdapter extends AbstractModelAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider           认证提供者
     */
    protected AbstractVendorAdapter(RequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }

    @Override
    public <T> T convertRequest(ModelRequest request, Class<T> vendorRequestType) {
        try {
            JSONObject vendorRequest = new JSONObject();

            // 设置模型
            String model = getModelName(request.getParameters());
            vendorRequest.put("model", model);
            log.debug("{}设置模型: {}", getVendorName(), model);

            // 设置是否流式输出
            vendorRequest.put("stream", request.isStream());

            // 设置其他参数
            Map<String, Object> mappedParams = requestMappingStrategy.mapParameters(request.getParameters());
            for (Map.Entry<String, Object> entry : mappedParams.entrySet()) {
                // 跳过model参数，因为已经单独设置
                if (!"model".equals(entry.getKey())) {
                    vendorRequest.put(entry.getKey(), entry.getValue());
                    log.debug("{}设置参数: {} = {}", getVendorName(), entry.getKey(), entry.getValue());
                }
            }

            // 根据请求类型设置消息或提示词
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                log.debug("{}使用消息模式，消息数量: {}", getVendorName(), request.getMessages().size());
                JSONArray messagesArray = convertMessages(request.getMessages());
                vendorRequest.put("messages", messagesArray);
            } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
                // 根据模型类型处理提示词
                log.debug("{}使用提示词模式，提示词长度: {}", getVendorName(), request.getPrompt().length());
                processPrompt(request.getPrompt(), vendorRequest, model);
            }

            // 添加工具相关配置
            if (request.getTools() != null && !request.getTools().isEmpty()) {
                // 将工具列表转换为JSON并添加到请求中
                vendorRequest.put("tools", JSON.toJSON(request.getTools()));
                log.debug("{}添加工具到请求，工具数量: {}", getVendorName(), request.getTools().size());
                log.debug("{}工具定义详情: {}", getVendorName(), JSON.toJSONString(request.getTools()));

                // 如果指定了工具选择策略，也添加到请求中
                if (request.getToolChoice() != null) {
                    vendorRequest.put("tool_choice", request.getToolChoice());
                    log.debug("{}设置工具选择策略: {}", getVendorName(), request.getToolChoice());
                } else {
                    // 默认使用"auto"，允许模型自行决定是否调用工具
                    vendorRequest.put("tool_choice", "auto");
                    log.debug("{}设置默认工具选择策略: auto", getVendorName());
                }
            }

            // 允许子类进行自定义处理
            customizeRequest(vendorRequest, request);

            log.debug("{}最终请求数据: {}", getVendorName(), vendorRequest);
            return (T) vendorRequest;
        } catch (Exception e) {
            log.error("转换{}请求失败", getVendorName(), e);
            throw new ModelException("转换" + getVendorName() + "请求失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> ModelResponse convertResponse(T vendorResponse) {
        try {
            if (vendorResponse instanceof String) {
                return parseStringResponse((String) vendorResponse);
            } else if (vendorResponse instanceof JSONObject) {
                return parseJsonResponse((JSONObject) vendorResponse);
            } else {
                throw new IllegalArgumentException("不支持的响应类型: " + vendorResponse.getClass().getName());
            }
        } catch (Exception e) {
            log.error("转换{}响应失败", getVendorName(), e);
            throw new ModelException("转换" + getVendorName() + "响应失败: " + e.getMessage(), e);
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
                            getVendorName() + " API错误: " + errorMessage,
                            vendorException,
                            modelErrorType,
                            errorCode
                    );
                }
            }
        } catch (Exception e) {
            // 如果解析失败，使用默认处理
            log.debug("解析{}错误失败，使用默认错误处理", getVendorName(), e);
        }

        return super.handleException(vendorException);
    }

    /**
     * 从参数中获取模型名称
     *
     * @param parameters 参数
     * @return 模型名称
     */
    protected String getModelName(Map<String, Object> parameters) {
        Object model = parameters.get("model");
        return model != null ? model.toString() : getDefaultModelName();
    }

    /**
     * 将消息列表转换为供应商特定的消息格式
     *
     * @param messages 消息列表
     * @return 供应商特定的消息数组
     */
    protected JSONArray convertMessages(List<Message> messages) {
        JSONArray messagesArray = new JSONArray();

        for (Message message : messages) {
            JSONObject vendorMessage = new JSONObject();

            // 转换角色
            vendorMessage.put("role", mapRole(message.getRole()));

            // 添加内容（如果非空）
            if (message.getContent() != null) {
                vendorMessage.put("content", message.getContent());
            }

            // 处理工具调用（assistant角色）
            if (message.getToolCalls() != null && !message.getToolCalls().isEmpty() &&
                    MessageRole.ASSISTANT.equals(message.getRole())) {
                vendorMessage.put("tool_calls", JSON.toJSON(message.getToolCalls()));
                log.debug("添加工具调用到assistant消息: {}", message.getToolCalls());
            }

            // 允许子类添加额外消息属性
            customizeMessage(vendorMessage, message);

            messagesArray.add(vendorMessage);
        }

        return messagesArray;
    }

    /**
     * 处理提示词
     *
     * @param prompt        提示词
     * @param vendorRequest 供应商请求
     * @param model         模型名称
     */
    protected void processPrompt(String prompt, JSONObject vendorRequest, String model) {
        // 默认转换为消息格式
        JSONArray messagesArray = new JSONArray();
        JSONObject userMessage = new JSONObject();
        userMessage.put("role", "user");
        userMessage.put("content", prompt);
        messagesArray.add(userMessage);
        vendorRequest.put("messages", messagesArray);
    }

    /**
     * 解析字符串响应
     *
     * @param response 响应字符串
     * @return 模型响应
     */
    protected ModelResponse parseStringResponse(String response) {
        try {
            JSONObject jsonResponse = JSON.parseObject(response);
            return parseJsonResponse(jsonResponse);
        } catch (Exception e) {
            // 如果不是JSON，则直接作为内容返回
            ModelResponse modelResponse = new ModelResponse();
            modelResponse.setContent(response);
            return modelResponse;
        }
    }

    /**
     * 解析JSON响应
     *
     * @param jsonResponse JSON响应
     * @return 模型响应
     */
    protected ModelResponse parseJsonResponse(JSONObject jsonResponse) {
        ModelResponse modelResponse = new ModelResponse();

        log.debug("原始JSON响应: {}", jsonResponse);

        // 处理完成响应
        if (jsonResponse.containsKey("choices")) {
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (!choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);
                log.debug("处理选择对象: {}", firstChoice);
                extractContent(firstChoice, modelResponse);
            }
        }

        // 设置响应ID
        modelResponse.setResponseId(jsonResponse.getString("id"));

        // 设置模型ID
        modelResponse.setModelId(jsonResponse.getString("model"));

        // 设置创建时间
        if (jsonResponse.containsKey("created")) {
            modelResponse.setCreatedAt(jsonResponse.getLongValue("created") * 1000); // 转换为毫秒
        } else {
            modelResponse.setCreatedAt(System.currentTimeMillis());
        }

        // 设置Token使用情况
        if (jsonResponse.containsKey("usage")) {
            JSONObject usage = jsonResponse.getJSONObject("usage");
            TokenUsage tokenUsage = TokenUsage.of(
                    usage.getIntValue("prompt_tokens"),
                    usage.getIntValue("completion_tokens")
            );
            modelResponse.setTokenUsage(tokenUsage);
        }

        // 设置原始响应
        modelResponse.setRawResponse((Map<String, Object>) JSON.toJSON(jsonResponse));

        // 允许子类进行自定义处理
        customizeResponse(modelResponse, jsonResponse);

        log.debug("最终解析的模型响应: {}", modelResponse);

        return modelResponse;
    }

    /**
     * 从选择对象中提取内容
     *
     * @param choice        选择对象
     * @param modelResponse 模型响应
     */
    protected void extractContent(JSONObject choice, ModelResponse modelResponse) {
        log.debug("{}开始提取内容，选择对象: {}", getVendorName(), choice);

        // 提取文本内容
        extractTextContent(choice, modelResponse);

        // 提取工具调用
        extractToolCalls(choice, modelResponse);

        log.debug("{}内容提取完成，最终内容: {}", getVendorName(), modelResponse.getContent());
    }

    /**
     * 提取文本内容（从不同格式的响应中）
     *
     * @param choice        选择对象
     * @param modelResponse 模型响应
     */
    protected void extractTextContent(JSONObject choice, ModelResponse modelResponse) {
        String content = null;

        // 1. 直接content字段 (Claude, OpenAI等)
        if (choice.containsKey("content")) {
            content = choice.getString("content");
            log.debug("{}从content字段提取内容: {}", getVendorName(), content);
        }
        // 2. text字段 (Ollama等)
        else if (choice.containsKey("text")) {
            content = choice.getString("text");
            log.debug("{}从text字段提取内容: {}", getVendorName(), content);
        }
        // 3. delta.content字段 (流式响应，通义等)
        else if (choice.containsKey("delta") && choice.getJSONObject("delta").containsKey("content")) {
            content = choice.getJSONObject("delta").getString("content");
            log.debug("{}从delta.content字段提取内容: {}", getVendorName(), content);
        }
        // 4. delta.text字段 (流式响应，某些模型)
        else if (choice.containsKey("delta") && choice.getJSONObject("delta").containsKey("text")) {
            content = choice.getJSONObject("delta").getString("text");
            log.debug("{}从delta.text字段提取内容: {}", getVendorName(), content);
        }
        // 5. message.content字段 (GPT等)
        else if (choice.containsKey("message") && choice.getJSONObject("message").containsKey("content")) {
            content = choice.getJSONObject("message").getString("content");
            log.debug("{}从message.content字段提取内容: {}", getVendorName(), content);
        }

        // 如果提取到内容，设置到响应中
        if (content != null) {
            modelResponse.setContent(content);
        } else {
            log.debug("{}未能从选择对象中提取文本内容", getVendorName());
        }
    }

    /**
     * 提取工具调用
     *
     * @param choice        选择对象
     * @param modelResponse 模型响应
     */
    protected void extractToolCalls(JSONObject choice, ModelResponse modelResponse) {
        try {
            List<ToolCall> toolCalls = null;

            // 1. 直接tool_calls字段
            if (choice.containsKey("tool_calls")) {
                JSONArray toolCallsArray = choice.getJSONArray("tool_calls");
                if (toolCallsArray != null && !toolCallsArray.isEmpty()) {
                    toolCalls = JSON.parseArray(toolCallsArray.toJSONString(), ToolCall.class);
                    log.debug("{}从tool_calls字段提取工具调用: {}", getVendorName(), toolCalls);
                }
            }
            // 2. message.tool_calls字段 (OpenAI等)
            else if (choice.containsKey("message") && choice.getJSONObject("message").containsKey("tool_calls")) {
                JSONArray toolCallsArray = choice.getJSONObject("message").getJSONArray("tool_calls");
                if (toolCallsArray != null && !toolCallsArray.isEmpty()) {
                    toolCalls = JSON.parseArray(toolCallsArray.toJSONString(), ToolCall.class);
                    log.debug("{}从message.tool_calls字段提取工具调用: {}", getVendorName(), toolCalls);
                }
            }
            // 3. delta.tool_calls字段 (流式响应)
            else if (choice.containsKey("delta") && choice.getJSONObject("delta").containsKey("tool_calls")) {
                JSONArray toolCallsArray = choice.getJSONObject("delta").getJSONArray("tool_calls");
                if (toolCallsArray != null && !toolCallsArray.isEmpty()) {
                    toolCalls = JSON.parseArray(toolCallsArray.toJSONString(), ToolCall.class);
                    log.debug("{}从delta.tool_calls字段提取工具调用: {}", getVendorName(), toolCalls);
                }
            }

            // 如果提取到工具调用，设置到响应中
            if (toolCalls != null && !toolCalls.isEmpty()) {
                modelResponse.setToolCalls(toolCalls);
            }
        } catch (Exception e) {
            log.error("{}解析工具调用时发生错误: {}", getVendorName(), e.getMessage(), e);
        }
    }

    /**
     * 将角色映射为供应商特定的角色值
     *
     * @param role 角色
     * @return 供应商特定的角色值
     */
    protected String mapRole(MessageRole role) {
        return switch (role) {
            case SYSTEM -> "system";
            case ASSISTANT -> "assistant";
            case FUNCTION -> "function";
            case TOOL -> "tool";
            default -> "user";
        };
    }

    /**
     * 映射错误类型到内部错误类型
     *
     * @param errorType 供应商错误类型
     * @return 内部错误类型
     */
    protected abstract ErrorType mapErrorType(String errorType);

    /**
     * 获取供应商名称
     *
     * @return 供应商名称
     */
    protected abstract String getVendorName();

    /**
     * 获取默认模型名称
     *
     * @return 默认模型名称
     */
    protected abstract String getDefaultModelName();

    /**
     * 自定义请求对象，供子类重写
     *
     * @param vendorRequest   供应商请求对象
     * @param originalRequest 原始请求
     */
    protected void customizeRequest(JSONObject vendorRequest, ModelRequest originalRequest) {
        // 默认不做额外处理
    }

    /**
     * 自定义消息对象，供子类重写
     *
     * @param vendorMessage   供应商消息对象
     * @param originalMessage 原始消息
     */
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        // 处理工具和函数相关字段
        if (originalMessage.getName() != null) {
            vendorMessage.put("name", originalMessage.getName());
        }

        if (originalMessage.getToolCallId() != null) {
            vendorMessage.put("tool_call_id", originalMessage.getToolCallId());
            log.debug("设置工具调用ID: {}", originalMessage.getToolCallId());
        }
    }

    /**
     * 自定义响应对象，供子类重写
     *
     * @param modelResponse 模型响应对象
     * @param jsonResponse  JSON响应
     */
    protected void customizeResponse(ModelResponse modelResponse, JSONObject jsonResponse) {
        // 默认不做额外处理
    }
}
