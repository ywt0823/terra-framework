package com.terra.framework.nova.model.openai;

import com.terra.framework.nova.model.RequestMappingStrategy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * OpenAI请求参数映射策略
 *
 * @author terra-nova
 */
public class OpenAIRequestMappingStrategy implements RequestMappingStrategy {

    /**
     * 参数映射表
     */
    private static final Map<String, String> PARAM_MAPPING = new HashMap<>();

    static {
        // 基础参数映射
        PARAM_MAPPING.put("temperature", "temperature");
        PARAM_MAPPING.put("top_p", "top_p");
        PARAM_MAPPING.put("top_k", "top_k");
        PARAM_MAPPING.put("frequency_penalty", "frequency_penalty");
        PARAM_MAPPING.put("presence_penalty", "presence_penalty");
        PARAM_MAPPING.put("max_tokens", "max_tokens");
        PARAM_MAPPING.put("stop", "stop");
        PARAM_MAPPING.put("n", "n");
        PARAM_MAPPING.put("seed", "seed");
        PARAM_MAPPING.put("stream", "stream");
        PARAM_MAPPING.put("user", "user");
        PARAM_MAPPING.put("logit_bias", "logit_bias");

        // OpenAI特有参数
        PARAM_MAPPING.put("response_format", "response_format");
        PARAM_MAPPING.put("functions", "functions");
        PARAM_MAPPING.put("tools", "tools");
        PARAM_MAPPING.put("tool_choice", "tool_choice");
    }

    @Override
    public Map<String, Object> mapParameters(Map<String, Object> genericParams) {
        Map<String, Object> openaiParams = new HashMap<>();

        if (genericParams == null || genericParams.isEmpty()) {
            return openaiParams;
        }

        // 处理模型名称
        processModelName(genericParams, openaiParams);

        // 处理通用参数
        for (Map.Entry<String, Object> entry : genericParams.entrySet()) {
            String key = entry.getKey();
            String mappedKey = PARAM_MAPPING.get(key);

            if (mappedKey != null) {
                openaiParams.put(mappedKey, entry.getValue());
            }
        }

        // 处理stop参数特殊情况（可能是字符串或数组）
        processStopParam(genericParams, openaiParams);

        // 处理响应格式特殊情况
        processResponseFormat(genericParams, openaiParams);

        return openaiParams;
    }

    /**
     * 处理模型名称
     *
     * @param genericParams 通用参数
     * @param openaiParams OpenAI参数
     */
    private void processModelName(Map<String, Object> genericParams, Map<String, Object> openaiParams) {
        Object model = genericParams.get("model");
        if (model != null) {
            String modelStr = model.toString();

            // 如果模型名带有前缀（如openai:gpt-4），则截取真实模型名
            if (modelStr.contains(":")) {
                modelStr = modelStr.substring(modelStr.indexOf(":") + 1);
            }

            openaiParams.put("model", modelStr);
        } else {
            // 默认模型
            openaiParams.put("model", "gpt-3.5-turbo");
        }
    }

    /**
     * 处理停止词参数
     *
     * @param genericParams 通用参数
     * @param openaiParams OpenAI参数
     */
    @SuppressWarnings("unchecked")
    private void processStopParam(Map<String, Object> genericParams, Map<String, Object> openaiParams) {
        Object stopObj = genericParams.get("stop");
        if (stopObj != null) {
            if (stopObj instanceof String) {
                // 如果是单个字符串，则创建只包含一个元素的数组
                openaiParams.put("stop", new String[]{(String) stopObj});
            } else if (stopObj instanceof List) {
                // 如果已经是列表，则直接使用
                openaiParams.put("stop", ((List<String>) stopObj).toArray(new String[0]));
            } else if (stopObj instanceof String[]) {
                // 如果已经是数组，则直接使用
                openaiParams.put("stop", stopObj);
            }
        }
    }

    /**
     * 处理响应格式
     *
     * @param genericParams 通用参数
     * @param openaiParams OpenAI参数
     */
    private void processResponseFormat(Map<String, Object> genericParams, Map<String, Object> openaiParams) {
        Object formatObj = genericParams.get("response_format");
        if (formatObj != null) {
            if (formatObj instanceof String) {
                String format = (String) formatObj;
                // 如果是字符串，转换为OpenAI的格式对象
                if ("json".equalsIgnoreCase(format)) {
                    Map<String, String> responseFormat = new HashMap<>();
                    responseFormat.put("type", "json_object");
                    openaiParams.put("response_format", responseFormat);
                } else if ("text".equalsIgnoreCase(format)) {
                    Map<String, String> responseFormat = new HashMap<>();
                    responseFormat.put("type", "text");
                    openaiParams.put("response_format", responseFormat);
                }
            } else {
                // 如果已经是对象/映射，则直接使用
                openaiParams.put("response_format", formatObj);
            }
        }
    }
}
