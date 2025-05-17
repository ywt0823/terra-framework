package com.terra.framework.nova.core.model.deepseek;

import com.terra.framework.nova.core.model.RequestMappingStrategy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DeepSeek请求参数映射策略
 *
 * @author terra-nova
 */
public class DeepSeekRequestMappingStrategy implements RequestMappingStrategy {

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
        PARAM_MAPPING.put("stream", "stream");
        PARAM_MAPPING.put("user", "user");
        PARAM_MAPPING.put("seed", "seed");

        // DeepSeek特有参数
        PARAM_MAPPING.put("tools", "tools");
        PARAM_MAPPING.put("tool_choice", "tool_choice");
        PARAM_MAPPING.put("safe_mode", "safe_mode");
    }

    @Override
    public Map<String, Object> mapParameters(Map<String, Object> genericParams) {
        Map<String, Object> deepseekParams = new HashMap<>();

        if (genericParams == null || genericParams.isEmpty()) {
            return deepseekParams;
        }

        // 处理模型名称
        processModelName(genericParams, deepseekParams);

        // 处理通用参数
        for (Map.Entry<String, Object> entry : genericParams.entrySet()) {
            String key = entry.getKey();
            String mappedKey = PARAM_MAPPING.get(key);

            if (mappedKey != null) {
                deepseekParams.put(mappedKey, entry.getValue());
            }
        }

        // 处理stop参数特殊情况
        processStopParam(genericParams, deepseekParams);

        // 处理响应格式
        processResponseFormat(genericParams, deepseekParams);

        return deepseekParams;
    }

    /**
     * 处理模型名称
     *
     * @param genericParams 通用参数
     * @param deepseekParams DeepSeek参数
     */
    private void processModelName(Map<String, Object> genericParams, Map<String, Object> deepseekParams) {
        Object model = genericParams.get("model");
        if (model != null) {
            String modelStr = model.toString();

            // 如果模型名带有前缀（如deepseek:deepseek-chat），则截取真实模型名
            if (modelStr.contains(":")) {
                modelStr = modelStr.substring(modelStr.indexOf(":") + 1);
            }

            deepseekParams.put("model", modelStr);
        } else {
            // 默认模型
            deepseekParams.put("model", "deepseek-chat");
        }
    }

    /**
     * 处理停止词参数
     *
     * @param genericParams 通用参数
     * @param deepseekParams DeepSeek参数
     */
    @SuppressWarnings("unchecked")
    private void processStopParam(Map<String, Object> genericParams, Map<String, Object> deepseekParams) {
        Object stopObj = genericParams.get("stop");
        if (stopObj != null) {
            if (stopObj instanceof String) {
                // 如果是单个字符串，则创建只包含一个元素的数组
                deepseekParams.put("stop", new String[]{(String) stopObj});
            } else if (stopObj instanceof List) {
                // 如果已经是列表，则直接使用
                deepseekParams.put("stop", ((List<String>) stopObj).toArray(new String[0]));
            } else if (stopObj instanceof String[]) {
                // 如果已经是数组，则直接使用
                deepseekParams.put("stop", stopObj);
            }
        }
    }

    /**
     * 处理响应格式
     *
     * @param genericParams 通用参数
     * @param deepseekParams DeepSeek参数
     */
    private void processResponseFormat(Map<String, Object> genericParams, Map<String, Object> deepseekParams) {
        Object formatObj = genericParams.get("response_format");
        if (formatObj != null) {
            if (formatObj instanceof String) {
                String format = (String) formatObj;
                if ("json".equalsIgnoreCase(format)) {
                    // 对于JSON格式，使用DeepSeek的响应格式
                    Map<String, String> responseFormat = new HashMap<>();
                    responseFormat.put("type", "json_object");
                    deepseekParams.put("response_format", responseFormat);
                }
            } else if (formatObj instanceof Map) {
                // 如果已经是对象/映射，则直接使用
                deepseekParams.put("response_format", formatObj);
            }
        }
    }
}
