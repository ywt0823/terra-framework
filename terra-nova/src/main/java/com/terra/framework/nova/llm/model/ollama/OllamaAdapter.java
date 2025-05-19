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

import java.util.List;
import java.util.Map;
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
        // 特殊处理function或tool角色
        if (originalMessage.getRole() == MessageRole.FUNCTION 
            || originalMessage.getRole() == MessageRole.TOOL) {
            // Ollama可能不支持function或tool角色，但为了兼容性，我们将其转换为assistant
            vendorMessage.put("role", "assistant");
            vendorMessage.put("content", "[工具调用结果] " + originalMessage.getContent());
        }
    }

    @Override
    protected void extractContent(JSONObject choice, ModelResponse modelResponse) {
        // Ollama使用不同的字段
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
