package com.terra.framework.nova.llm.model.ollama;

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
 * Ollama模型适配器
 *
 * @author terra-nova
 */
@Slf4j
public class OllamaAdapter extends AbstractModelAdapter {

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
    public <T> T convertRequest(ModelRequest request, Class<T> vendorRequestType) {
        try {
            JSONObject ollamaRequest = new JSONObject();

            // 设置模型
            String model = getModelName(request.getParameters());
            ollamaRequest.put("model", model);

            // 设置是否流式输出
            ollamaRequest.put("stream", request.isStream());

            // 设置其他参数
            Map<String, Object> mappedParams = requestMappingStrategy.mapParameters(request.getParameters());
            for (Map.Entry<String, Object> entry : mappedParams.entrySet()) {
                // 跳过model参数，因为已经单独设置
                if (!"model".equals(entry.getKey())) {
                    ollamaRequest.put(entry.getKey(), entry.getValue());
                }
            }

            // 根据请求类型设置消息或提示词
            if (request.getMessages() != null && !request.getMessages().isEmpty()) {
                // 如果使用chat/completions接口
                if (isChatCompletions(request.getParameters())) {
                    JSONArray messagesArray = convertMessages(request.getMessages());
                    ollamaRequest.put("messages", messagesArray);
                } else {
                    // 使用generate接口，将消息转换为提示文本
                    ollamaRequest.put("prompt", convertMessagesToPrompt(request.getMessages()));
                }
            } else if (request.getPrompt() != null && !request.getPrompt().isEmpty()) {
                // 对于生成式请求，直接使用prompt
                ollamaRequest.put("prompt", request.getPrompt());
            }

            return (T) ollamaRequest;
        } catch (Exception e) {
            log.error("转换Ollama请求失败", e);
            throw new ModelException("转换Ollama请求失败: " + e.getMessage(), e);
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
            log.error("转换Ollama响应失败", e);
            throw new ModelException("转换Ollama响应失败: " + e.getMessage(), e);
        }
    }

    @Override
    public ModelException handleException(Exception vendorException) {
        try {
            if (vendorException.getMessage() != null && vendorException.getMessage().contains("\"error\":")) {
                JSONObject errorJson = JSON.parseObject(vendorException.getMessage());
                if (errorJson.containsKey("error")) {
                    String errorMessage = errorJson.getString("error");

                    ErrorType modelErrorType = mapErrorType(errorMessage);
                    return new ModelException(
                            "Ollama API错误: " + errorMessage,
                            vendorException,
                            modelErrorType
                    );
                }
            }
        } catch (Exception e) {
            // 如果解析失败，使用默认处理
            log.debug("解析Ollama错误失败，使用默认错误处理", e);
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
        if (model != null) {
            String modelStr = model.toString();

            // 如果模型名带有前缀（如ollama:llama2），则截取真实模型名
            if (modelStr.contains(":")) {
                modelStr = modelStr.substring(modelStr.indexOf(":") + 1);
            }

            return modelStr;
        }
        return "llama2";  // 默认使用Llama2模型
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
     * 将消息列表转换为Ollama消息格式
     *
     * @param messages 消息列表
     * @return Ollama消息数组
     */
    private JSONArray convertMessages(List<Message> messages) {
        JSONArray messagesArray = new JSONArray();

        for (Message message : messages) {
            JSONObject ollamaMessage = new JSONObject();

            // 转换角色
            switch (message.getRole()) {
                case SYSTEM:
                    ollamaMessage.put("role", "system");
                    break;
                case USER:
                    ollamaMessage.put("role", "user");
                    break;
                case ASSISTANT:
                    ollamaMessage.put("role", "assistant");
                    break;
                case FUNCTION:
                case TOOL:
                    // Ollama可能不支持function或tool角色，但为了兼容性，我们将其转换为assistant
                    ollamaMessage.put("role", "assistant");
                    ollamaMessage.put("content", "[工具调用结果] " + message.getContent());
                    break;
                default:
                    ollamaMessage.put("role", "user");
            }

            // 如果是标准用户、系统或助手角色，直接设置内容
            if (!ollamaMessage.containsKey("content")) {
                ollamaMessage.put("content", message.getContent());
            }

            messagesArray.add(ollamaMessage);
        }

        return messagesArray;
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

        // 解析不同类型的响应
        if (jsonResponse.containsKey("response")) {
            // generate接口的响应
            modelResponse.setContent(jsonResponse.getString("response"));
        } else if (jsonResponse.containsKey("message")) {
            // chat接口的响应
            JSONObject message = jsonResponse.getJSONObject("message");
            modelResponse.setContent(message.getString("content"));
        } else if (jsonResponse.containsKey("content")) {
            // 流式响应中的内容
            modelResponse.setContent(jsonResponse.getString("content"));
        }

        // 设置模型ID
        if (jsonResponse.containsKey("model")) {
            modelResponse.setModelId(jsonResponse.getString("model"));
        }

        // 设置响应ID (Ollama可能不提供这个)
        modelResponse.setResponseId(String.valueOf(System.currentTimeMillis()));

        // 设置创建时间
        modelResponse.setCreatedAt(System.currentTimeMillis());

        // 设置Token使用情况（如果有）
        if (jsonResponse.containsKey("prompt_eval_count") && jsonResponse.containsKey("eval_count")) {
            TokenUsage tokenUsage = TokenUsage.of(
                    jsonResponse.getIntValue("prompt_eval_count"),
                    jsonResponse.getIntValue("eval_count")
            );
            modelResponse.setTokenUsage(tokenUsage);
        }

        // 设置原始响应
        modelResponse.setRawResponse((Map<String, Object>) JSON.toJSON(jsonResponse));

        return modelResponse;
    }

    /**
     * 映射Ollama错误消息到内部错误类型
     *
     * @param errorMessage Ollama错误消息
     * @return 内部错误类型
     */
    private ErrorType mapErrorType(String errorMessage) {
        if (errorMessage == null) {
            return ErrorType.UNKNOWN_ERROR;
        }

        errorMessage = errorMessage.toLowerCase();

        if (errorMessage.contains("auth") || errorMessage.contains("unauthorized") || errorMessage.contains("token")) {
            return ErrorType.AUTHENTICATION_ERROR;
        } else if (errorMessage.contains("rate") || errorMessage.contains("limit")) {
            return ErrorType.RATE_LIMIT_ERROR;
        } else if (errorMessage.contains("context") || errorMessage.contains("length") || errorMessage.contains("too large")) {
            return ErrorType.CONTEXT_LENGTH_ERROR;
        } else if (errorMessage.contains("invalid") || errorMessage.contains("parameter") || errorMessage.contains("format")) {
            return ErrorType.INVALID_REQUEST_ERROR;
        } else if (errorMessage.contains("server") || errorMessage.contains("internal")) {
            return ErrorType.SERVER_ERROR;
        } else if (errorMessage.contains("not found") || errorMessage.contains("model") || errorMessage.contains("unavailable")) {
            return ErrorType.MODEL_UNAVAILABLE_ERROR;
        } else if (errorMessage.contains("content") || errorMessage.contains("filter") || errorMessage.contains("moderation")) {
            return ErrorType.CONTENT_FILTER_ERROR;
        } else {
            return ErrorType.UNKNOWN_ERROR;
        }
    }
}
