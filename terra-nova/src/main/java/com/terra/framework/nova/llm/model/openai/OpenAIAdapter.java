package com.terra.framework.nova.llm.model.openai;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.exception.ErrorType;
import com.terra.framework.nova.llm.exception.ModelException;
import com.terra.framework.nova.llm.model.AbstractModelAdapter;
import com.terra.framework.nova.llm.model.AuthProvider;
import com.terra.framework.nova.llm.model.Message;
import com.terra.framework.nova.llm.model.ModelRequest;
import com.terra.framework.nova.llm.model.ModelResponse;
import com.terra.framework.nova.llm.model.TokenUsage;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenAI模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class OpenAIAdapter extends AbstractModelAdapter {

    /**
     * 构造函数
     *
     * @param requestMappingStrategy 请求参数映射策略
     * @param authProvider 认证提供者
     */
    public OpenAIAdapter(OpenAIRequestMappingStrategy requestMappingStrategy, AuthProvider authProvider) {
        super(requestMappingStrategy, authProvider);
    }

    @Override
    public <T> T convertRequest(ModelRequest request, Class<T> vendorRequestType) {
        try {
            JSONObject openaiRequest = new JSONObject();

            // 设置模型
            String model = getModelName(request.getParameters());
            openaiRequest.put("model", model);

            // 设置是否流式输出
            openaiRequest.put("stream", request.isStream());

            // 设置其他参数
            Map<String, Object> mappedParams = requestMappingStrategy.mapParameters(request.getParameters());
            for (Map.Entry<String, Object> entry : mappedParams.entrySet()) {
                // 跳过model参数，因为已经单独设置
                if (!"model".equals(entry.getKey())) {
                    openaiRequest.put(entry.getKey(), entry.getValue());
                }
            }

            // 根据请求类型设置消息或提示词
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                JSONArray messagesArray = convertMessages(request.getMessages());
                openaiRequest.put("messages", messagesArray);
            } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
                // 对于纯文本模型，使用prompt参数
                if (model.startsWith("text-")) {
                    openaiRequest.put("prompt", request.getPrompt());
                } else {
                    // 对于聊天模型，将提示词转换为消息
                    JSONArray messagesArray = new JSONArray();
                    JSONObject userMessage = new JSONObject();
                    userMessage.put("role", "user");
                    userMessage.put("content", request.getPrompt());
                    messagesArray.add(userMessage);
                    openaiRequest.put("messages", messagesArray);
                }
            }

            return (T) openaiRequest;
        } catch (Exception e) {
            log.error("转换OpenAI请求失败", e);
            throw new ModelException("转换OpenAI请求失败: " + e.getMessage(), e);
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
            log.error("转换OpenAI响应失败", e);
            throw new ModelException("转换OpenAI响应失败: " + e.getMessage(), e);
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
                            "OpenAI API错误: " + errorMessage,
                            vendorException,
                            modelErrorType,
                            errorCode
                    );
                }
            }
        } catch (Exception e) {
            // 如果解析失败，使用默认处理
            log.debug("解析OpenAI错误失败，使用默认错误处理", e);
        }

        return super.handleException(vendorException);
    }

    /**
     * 从参数中获取模型名称
     *
     * @param parameters 参数
     * @return 模型名称
     */
    private String getModelName(Map<String, Object> parameters) {
        Object model = parameters.get("model");
        return model != null ? model.toString() : "gpt-3.5-turbo";
    }

    /**
     * 将消息列表转换为OpenAI消息格式
     *
     * @param messages 消息列表
     * @return OpenAI消息数组
     */
    private JSONArray convertMessages(List<Message> messages) {
        JSONArray messagesArray = new JSONArray();

        for (Message message : messages) {
            JSONObject openaiMessage = new JSONObject();

            // 转换角色
            switch (message.getRole()) {
                case SYSTEM:
                    openaiMessage.put("role", "system");
                    break;
                case USER:
                    openaiMessage.put("role", "user");
                    break;
                case ASSISTANT:
                    openaiMessage.put("role", "assistant");
                    break;
                case FUNCTION:
                    openaiMessage.put("role", "function");
                    break;
                case TOOL:
                    openaiMessage.put("role", "tool");
                    break;
                default:
                    openaiMessage.put("role", "user");
            }

            openaiMessage.put("content", message.getContent());
            messagesArray.add(openaiMessage);
        }

        return messagesArray;
    }

    /**
     * 解析字符串响应
     *
     * @param response 响应字符串
     * @return 模型响应
     */
    private ModelResponse parseStringResponse(String response) {
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
    private ModelResponse parseJsonResponse(JSONObject jsonResponse) {
        ModelResponse modelResponse = new ModelResponse();

        // 处理聊天完成响应
        if (jsonResponse.containsKey("choices")) {
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (!choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);

                // 提取内容
                if (firstChoice.containsKey("message")) {
                    JSONObject message = firstChoice.getJSONObject("message");
                    modelResponse.setContent(message.getString("content"));
                } else if (firstChoice.containsKey("text")) {
                    modelResponse.setContent(firstChoice.getString("text"));
                } else if (firstChoice.containsKey("delta") &&
                           firstChoice.getJSONObject("delta").containsKey("content")) {
                    modelResponse.setContent(firstChoice.getJSONObject("delta").getString("content"));
                }
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

        return modelResponse;
    }

    /**
     * 映射OpenAI错误类型到内部错误类型
     *
     * @param openaiErrorType OpenAI错误类型
     * @return 内部错误类型
     */
    private ErrorType mapErrorType(String openaiErrorType) {
        if (openaiErrorType == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        switch (openaiErrorType) {
            case "authentication_error":
                return ErrorType.AUTHENTICATION_ERROR;
            case "rate_limit_exceeded":
                return ErrorType.RATE_LIMIT_ERROR;
            case "context_length_exceeded":
                return ErrorType.CONTEXT_LENGTH_ERROR;
            case "invalid_request_error":
                return ErrorType.INVALID_REQUEST_ERROR;
            case "server_error":
                return ErrorType.SERVER_ERROR;
            case "model_overloaded":
            case "model_not_found":
                return ErrorType.MODEL_UNAVAILABLE_ERROR;
            case "content_filter":
                return ErrorType.CONTENT_FILTER_ERROR;
            default:
                return ErrorType.UNKNOWN_ERROR;
        }
    }
}
