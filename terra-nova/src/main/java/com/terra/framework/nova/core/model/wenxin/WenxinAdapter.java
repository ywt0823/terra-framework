package com.terra.framework.nova.core.model.wenxin;

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
 * 文心一言模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class WenxinAdapter extends AbstractModelAdapter {

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
    public <T> T convertRequest(ModelRequest request, Class<T> vendorRequestType) {
        try {
            JSONObject wenxinRequest = new JSONObject();

            // 文心一言不在请求中指定模型，而是通过URL路径指定

            // 设置是否流式输出
            if (request.isStream()) {
                wenxinRequest.put("stream", true);
            }

            // 设置其他参数
            Map<String, Object> mappedParams = requestMappingStrategy.mapParameters(request.getParameters());
            for (Map.Entry<String, Object> entry : mappedParams.entrySet()) {
                wenxinRequest.put(entry.getKey(), entry.getValue());
            }

            // 根据请求类型设置消息或提示词
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                JSONArray messagesArray = convertMessages(request.getMessages());
                wenxinRequest.put("messages", messagesArray);
            } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
                // 将提示词转换为消息
                JSONArray messagesArray = new JSONArray();
                JSONObject userMessage = new JSONObject();
                userMessage.put("role", "user");
                userMessage.put("content", request.getPrompt());
                messagesArray.add(userMessage);
                wenxinRequest.put("messages", messagesArray);
            }

            return (T) wenxinRequest;
        } catch (Exception e) {
            log.error("转换文心一言请求失败", e);
            throw new ModelException("转换文心一言请求失败: " + e.getMessage(), e);
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
            log.error("转换文心一言响应失败", e);
            throw new ModelException("转换文心一言响应失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ModelException handleException(Exception vendorException) {
        try {
            if (vendorException.getMessage() != null) {
                JSONObject errorJson = JSON.parseObject(vendorException.getMessage());
                if (errorJson.containsKey("error_code")) {
                    String errorCode = errorJson.getString("error_code");
                    String errorMessage = errorJson.getString("error_msg");

                    ErrorType modelErrorType = mapErrorType(errorCode);
                    return new ModelException(
                            "文心一言API错误: " + errorMessage,
                            vendorException,
                            modelErrorType,
                            errorCode
                    );
                }
            }
        } catch (Exception e) {
            // 如果解析失败，使用默认处理
            log.debug("解析文心一言错误失败，使用默认错误处理", e);
        }

        return super.handleException(vendorException);
    }

    /**
     * 将消息列表转换为文心一言消息格式
     *
     * @param messages 消息列表
     * @return 文心一言消息数组
     */
    private JSONArray convertMessages(List<Message> messages) {
        JSONArray messagesArray = new JSONArray();

        for (Message message : messages) {
            JSONObject wenxinMessage = new JSONObject();

            // 转换角色
            switch (message.getRole()) {
                case SYSTEM:
                    // 文心一言不支持system角色，将其转换为user角色并添加标记
                    wenxinMessage.put("role", "user");
                    wenxinMessage.put("content", "[系统指令]" + message.getContent());
                    break;
                case USER:
                    wenxinMessage.put("role", "user");
                    wenxinMessage.put("content", message.getContent());
                    break;
                case ASSISTANT:
                    wenxinMessage.put("role", "assistant");
                    wenxinMessage.put("content", message.getContent());
                    break;
                case FUNCTION:
                case TOOL:
                    // 文心一言不直接支持function或tool角色，将其转换为附加信息
                    wenxinMessage.put("role", "assistant");
                    wenxinMessage.put("content", "工具调用结果: " + message.getContent());
                    break;
                default:
                    wenxinMessage.put("role", "user");
                    wenxinMessage.put("content", message.getContent());
            }

            messagesArray.add(wenxinMessage);
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

        // 文心一言的标准响应格式
        if (jsonResponse.containsKey("result")) {
            // 非流式响应
            modelResponse.setContent(jsonResponse.getString("result"));
        } else if (jsonResponse.containsKey("delta")) {
            // 流式响应
            modelResponse.setContent(jsonResponse.getString("delta"));
        }

        // 设置响应ID
        modelResponse.setResponseId(jsonResponse.getString("id"));

        // 设置模型ID
        if (jsonResponse.containsKey("object")) {
            modelResponse.setModelId(jsonResponse.getString("object"));
        }

        // 设置创建时间
        long now = System.currentTimeMillis();
        modelResponse.setCreatedAt(now);

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
     * 映射文心一言错误码到内部错误类型
     *
     * @param errorCode 文心一言错误码
     * @return 内部错误类型
     */
    private ErrorType mapErrorType(String errorCode) {
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
}
