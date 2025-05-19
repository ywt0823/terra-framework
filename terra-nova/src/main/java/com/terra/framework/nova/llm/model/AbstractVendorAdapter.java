package com.terra.framework.nova.llm.model;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.exception.ModelException;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

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
     * @param authProvider 认证提供者
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

            // 设置是否流式输出
            vendorRequest.put("stream", request.isStream());

            // 设置其他参数
            Map<String, Object> mappedParams = requestMappingStrategy.mapParameters(request.getParameters());
            for (Map.Entry<String, Object> entry : mappedParams.entrySet()) {
                // 跳过model参数，因为已经单独设置
                if (!"model".equals(entry.getKey())) {
                    vendorRequest.put(entry.getKey(), entry.getValue());
                }
            }

            // 根据请求类型设置消息或提示词
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                JSONArray messagesArray = convertMessages(request.getMessages());
                vendorRequest.put("messages", messagesArray);
            } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
                // 根据模型类型处理提示词
                processPrompt(request.getPrompt(), vendorRequest, model);
            }

            // 允许子类进行自定义处理
            customizeRequest(vendorRequest, request);

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
            vendorMessage.put("content", message.getContent());
            
            // 允许子类添加额外消息属性
            customizeMessage(vendorMessage, message);
            
            messagesArray.add(vendorMessage);
        }

        return messagesArray;
    }

    /**
     * 处理提示词
     *
     * @param prompt 提示词
     * @param vendorRequest 供应商请求
     * @param model 模型名称
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

        // 处理完成响应
        if (jsonResponse.containsKey("choices")) {
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (!choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);
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

        return modelResponse;
    }

    /**
     * 从选择对象中提取内容
     *
     * @param choice 选择对象
     * @param modelResponse 模型响应
     */
    protected void extractContent(JSONObject choice, ModelResponse modelResponse) {
        if (choice.containsKey("message")) {
            JSONObject message = choice.getJSONObject("message");
            modelResponse.setContent(message.getString("content"));
        } else if (choice.containsKey("text")) {
            modelResponse.setContent(choice.getString("text"));
        } else if (choice.containsKey("delta") &&
                 choice.getJSONObject("delta").containsKey("content")) {
            modelResponse.setContent(choice.getJSONObject("delta").getString("content"));
        }
    }

    /**
     * 将角色映射为供应商特定的角色值
     *
     * @param role 角色
     * @return 供应商特定的角色值
     */
    protected String mapRole(MessageRole role) {
        switch (role) {
            case SYSTEM:
                return "system";
            case USER:
                return "user";
            case ASSISTANT:
                return "assistant";
            case FUNCTION:
                return "function";
            case TOOL:
                return "tool";
            default:
                return "user";
        }
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
     * @param vendorRequest 供应商请求对象
     * @param originalRequest 原始请求
     */
    protected void customizeRequest(JSONObject vendorRequest, ModelRequest originalRequest) {
        // 默认不做额外处理
    }

    /**
     * 自定义消息对象，供子类重写
     *
     * @param vendorMessage 供应商消息对象
     * @param originalMessage 原始消息
     */
    protected void customizeMessage(JSONObject vendorMessage, Message originalMessage) {
        // 默认不做额外处理
    }

    /**
     * 自定义响应对象，供子类重写
     *
     * @param modelResponse 模型响应对象
     * @param jsonResponse JSON响应
     */
    protected void customizeResponse(ModelResponse modelResponse, JSONObject jsonResponse) {
        // 默认不做额外处理
    }
} 