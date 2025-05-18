package com.terra.framework.nova.llm.model.ollama;

import com.terra.framework.nova.llm.model.RequestMappingStrategy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ollama请求参数映射策略
 *
 * @author terra-nova
 */
public class OllamaRequestMappingStrategy implements RequestMappingStrategy {

    /**
     * 参数映射表
     */
    private static final Map<String, String> PARAM_MAPPING = new HashMap<>();

    static {
        // 基础参数映射
        PARAM_MAPPING.put("temperature", "temperature");
        PARAM_MAPPING.put("top_p", "top_p");
        PARAM_MAPPING.put("top_k", "top_k");
        PARAM_MAPPING.put("max_tokens", "num_predict");  // Ollama使用num_predict表示最大生成token数
        PARAM_MAPPING.put("stop", "stop");
        PARAM_MAPPING.put("stream", "stream");
        PARAM_MAPPING.put("seed", "seed");

        // Ollama特有参数
        PARAM_MAPPING.put("mirostat", "mirostat");
        PARAM_MAPPING.put("mirostat_eta", "mirostat_eta");
        PARAM_MAPPING.put("mirostat_tau", "mirostat_tau");
        PARAM_MAPPING.put("num_ctx", "num_ctx");
        PARAM_MAPPING.put("num_gpu", "num_gpu");
        PARAM_MAPPING.put("num_thread", "num_thread");
        PARAM_MAPPING.put("repeat_penalty", "repeat_penalty");
        PARAM_MAPPING.put("tfs_z", "tfs_z");
        PARAM_MAPPING.put("num_batch", "num_batch");
        PARAM_MAPPING.put("numa", "numa");
        PARAM_MAPPING.put("presence_penalty", "presence_penalty");
        PARAM_MAPPING.put("frequency_penalty", "frequency_penalty");
        PARAM_MAPPING.put("system", "system");  // 系统提示，在chat API中才有效
    }

    @Override
    public Map<String, Object> mapParameters(Map<String, Object> genericParams) {
        Map<String, Object> ollamaParams = new HashMap<>();

        if (genericParams == null || genericParams.isEmpty()) {
            return ollamaParams;
        }

        // 处理模型名称
        processModelName(genericParams, ollamaParams);

        // 处理通用参数
        for (Map.Entry<String, Object> entry : genericParams.entrySet()) {
            String key = entry.getKey();
            String mappedKey = PARAM_MAPPING.get(key);

            if (mappedKey != null) {
                ollamaParams.put(mappedKey, entry.getValue());
            }
        }

        // 处理停止序列特殊情况
        processStopParam(genericParams, ollamaParams);

        // 处理API选择（generate vs chat）
        processApiChoice(genericParams, ollamaParams);

        return ollamaParams;
    }

    /**
     * 处理模型名称
     *
     * @param genericParams 通用参数
     * @param ollamaParams Ollama参数
     */
    private void processModelName(Map<String, Object> genericParams, Map<String, Object> ollamaParams) {
        Object model = genericParams.get("model");
        if (model != null) {
            String modelStr = model.toString();

            // 如果模型名带有前缀（如ollama:llama2），则截取真实模型名
            if (modelStr.contains(":")) {
                modelStr = modelStr.substring(modelStr.indexOf(":") + 1);
            }

            ollamaParams.put("model", modelStr);
        } else {
            // 默认模型
            ollamaParams.put("model", "llama2");
        }
    }

    /**
     * 处理停止序列参数
     *
     * @param genericParams 通用参数
     * @param ollamaParams Ollama参数
     */
    @SuppressWarnings("unchecked")
    private void processStopParam(Map<String, Object> genericParams, Map<String, Object> ollamaParams) {
        Object stopObj = genericParams.get("stop");
        if (stopObj != null) {
            if (stopObj instanceof String) {
                // 如果是单个字符串，则创建只包含一个元素的数组
                ollamaParams.put("stop", new String[]{(String) stopObj});
            } else if (stopObj instanceof List) {
                // 如果已经是列表，则直接使用
                ollamaParams.put("stop", ((List<String>) stopObj).toArray(new String[0]));
            } else if (stopObj instanceof String[]) {
                // 如果已经是数组，则直接使用
                ollamaParams.put("stop", stopObj);
            }
        }
    }

    /**
     * 处理API选择（generate vs chat）
     *
     * @param genericParams 通用参数
     * @param ollamaParams Ollama参数
     */
    private void processApiChoice(Map<String, Object> genericParams, Map<String, Object> ollamaParams) {
        // use_chat_api参数用于指示是否使用chat API而不是generate API
        Object useChatObj = genericParams.get("use_chat_api");
        if (useChatObj != null) {
            boolean useChat;
            if (useChatObj instanceof Boolean) {
                useChat = (Boolean) useChatObj;
            } else {
                useChat = Boolean.parseBoolean(useChatObj.toString());
            }

            // 保留此参数，以便适配器可以决定使用哪个API
            ollamaParams.put("use_chat_api", useChat);
        } else {
            // 默认情况下，根据模型决定
            String model = ollamaParams.getOrDefault("model", "llama2").toString();
            // 判断模型是否支持聊天接口
            boolean useChat = model.contains("chat") || model.contains("instruct");
            ollamaParams.put("use_chat_api", useChat);
        }
    }
}
