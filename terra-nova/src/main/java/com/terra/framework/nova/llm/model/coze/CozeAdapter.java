package com.terra.framework.nova.llm.model.coze;

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
 * Coze模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class CozeAdapter extends AbstractModelAdapter {

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
    public <T> T convertRequest(ModelRequest request, Class<T> vendorRequestType) {
        try {
            JSONObject cozeRequest = new JSONObject();

            // 设置模型
            String model = request.getParameters().getOrDefault("model", "gpt-3.5-turbo").toString();
            cozeRequest.put("model", model);

            // 设置是否流式输出
            cozeRequest.put("stream", request.isStream());

            // 设置其他参数
            Map<String, Object> mappedParams = requestMappingStrategy.mapParameters(request.getParameters());
            for (Map.Entry<String, Object> entry : mappedParams.entrySet()) {
                // 跳过model参数，因为已经单独设置
                if (!"model".equals(entry.getKey())) {
                    cozeRequest.put(entry.getKey(), entry.getValue());
                }
            }

            // 根据请求类型设置消息或提示词
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                JSONArray messagesArray = convertMessages(request.getMessages());
                cozeRequest.put("messages", messagesArray);
            } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
                // 对于聊天模型，将提示词转换为消息
                JSONArray messagesArray = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", request.getPrompt());
                messagesArray.add(userMessage);
                cozeRequest.put("messages", messagesArray);
            }

            return (T) cozeRequest;
        } catch (Exception e) {
            log.error("转换Coze请求失败", e);
            throw new ModelException("转换Coze请求失败: " + e.getMessage(), e);
        }
    }

    @Override
    public <T> ModelResponse convertResponse(T response) {
        try {
            String jsonStr = response instanceof String ? (String) response : JSON.toJSONString(response);
            JSONObject jsonResponse = JSON.parseObject(jsonStr);
            
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
            modelResponse.setCreatedAt(System.currentTimeMillis());

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
        } catch (Exception e) {
            log.error("转换Coze响应失败", e);
            throw new ModelException("转换Coze响应失败: " + e.getMessage(), e);
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

    private JSONArray convertMessages(List<Message> messages) {
        JSONArray cozeMessages = new JSONArray();
        for (Message message : messages) {
            JSONObject cozeMessage = new JSONObject();
            cozeMessage.put("role", message.getRole().toString().toLowerCase());
            cozeMessage.put("content", message.getContent());
            cozeMessages.add(cozeMessage);
        }
        return cozeMessages;
    }

    /**
     * 映射Coze错误类型到内部错误类型
     *
     * @param cozeErrorType Coze错误类型
     * @return 内部错误类型
     */
    private ErrorType mapErrorType(String cozeErrorType) {
        if (cozeErrorType == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        switch (cozeErrorType) {
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
} 