package com.terra.framework.nova.core.model.tongyi;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.core.exception.ErrorType;
import com.terra.framework.nova.core.exception.ModelException;
import com.terra.framework.nova.core.model.AbstractModelAdapter;
import com.terra.framework.nova.core.model.AuthProvider;
import com.terra.framework.nova.core.model.Message;
import com.terra.framework.nova.core.model.ModelRequest;
import com.terra.framework.nova.core.model.ModelResponse;
import com.terra.framework.nova.core.model.TokenUsage;

import java.util.List;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;

/**
 * 通义千问模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class TongyiAdapter extends AbstractModelAdapter {

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
    public <T> T convertRequest(ModelRequest request, Class<T> vendorRequestType) {
        try {
            JSONObject tongyiRequest = new JSONObject();

            // 设置模型
            String model = getModelName(request.getParameters());
            tongyiRequest.put("model", model);

            // 设置是否流式输出
            tongyiRequest.put("stream", request.isStream());

            // 设置其他参数
            Map<String, Object> mappedParams = requestMappingStrategy.mapParameters(request.getParameters());
            for (Map.Entry<String, Object> entry : mappedParams.entrySet()) {
                // 跳过model参数，因为已经单独设置
                if (!"model".equals(entry.getKey())) {
                    tongyiRequest.put(entry.getKey(), entry.getValue());
                }
            }

            // 根据请求类型设置消息或提示词
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                JSONArray messagesArray = convertMessages(request.getMessages());
                tongyiRequest.put("messages", messagesArray);
            } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
                // 将提示词转换为消息
                JSONArray messagesArray = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", request.getPrompt());
                messagesArray.add(userMessage);
                tongyiRequest.put("messages", messagesArray);
            }

            return (T) tongyiRequest;
        } catch (Exception e) {
            log.error("转换通义千问请求失败", e);
            throw new ModelException("转换通义千问请求失败: " + e.getMessage(), e);
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
            log.error("转换通义千问响应失败", e);
            throw new ModelException("转换通义千问响应失败: " + e.getMessage(), e);
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

    /**
     * 从参数中获取模型名称
     *
     * @param parameters 参数
     * @return 模型名称
     */
    private String getModelName(Map<String, Object> parameters) {
        Object model = parameters.get("model");
        return model != null ? model.toString() : "qwen-turbo";
    }

    /**
     * 将消息列表转换为通义千问消息格式
     *
     * @param messages 消息列表
     * @return 通义千问消息数组
     */
    private JSONArray convertMessages(List<Message> messages) {
        JSONArray messagesArray = new JSONArray();

        for (Message message : messages) {
            JSONObject tongyiMessage = new JSONObject();

            // 转换角色
            switch (message.getRole()) {
                case SYSTEM:
                    tongyiMessage.put("role", "system");
                    break;
                case USER:
                    tongyiMessage.put("role", "user");
                    break;
                case ASSISTANT:
                    tongyiMessage.put("role", "assistant");
                    break;
                case FUNCTION:
                    // 通义千问支持函数调用
                    tongyiMessage.put("role", "function");
                    break;
                case TOOL:
                    // 将tool角色映射到function角色
                    tongyiMessage.put("role", "function");
                    break;
                default:
                    tongyiMessage.put("role", "user");
            }

            tongyiMessage.put("content", message.getContent());
            messagesArray.add(tongyiMessage);
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

        // 处理通义千问的完整响应格式
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
                    } else if (firstChoice.containsKey("delta") &&
                               firstChoice.getJSONObject("delta").containsKey("content")) {
                        // 处理流式响应
                        modelResponse.setContent(firstChoice.getJSONObject("delta").getString("content"));
                    }
                }
            }
        } else if (jsonResponse.containsKey("choices")) {
            // 直接处理choices格式
            JSONArray choices = jsonResponse.getJSONArray("choices");
            if (!choices.isEmpty()) {
                JSONObject firstChoice = choices.getJSONObject(0);

                // 提取内容
                if (firstChoice.containsKey("message")) {
                    JSONObject message = firstChoice.getJSONObject("message");
                    modelResponse.setContent(message.getString("content"));
                } else if (firstChoice.containsKey("delta") &&
                           firstChoice.getJSONObject("delta").containsKey("content")) {
                    // 处理流式响应
                    modelResponse.setContent(firstChoice.getJSONObject("delta").getString("content"));
                }
            }
        }

        // 设置响应ID
        if (jsonResponse.containsKey("request_id")) {
            modelResponse.setResponseId(jsonResponse.getString("request_id"));
        }

        // 设置模型ID
        if (jsonResponse.containsKey("model")) {
            modelResponse.setModelId(jsonResponse.getString("model"));
        }

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
    }

    /**
     * 映射通义千问错误码到内部错误类型
     *
     * @param errorCode 通义千问错误码
     * @return 内部错误类型
     */
    private ErrorType mapErrorType(String errorCode) {
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
}
