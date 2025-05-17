package com.terra.framework.nova.core.model.tongyi;

import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.core.model.RequestMappingStrategy;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 通义千问请求参数映射策略
 *
 * @author terra-nova
 */
public class TongyiRequestMappingStrategy implements RequestMappingStrategy {

    /**
     * 参数映射表
     */
    private static final Map<String, String> PARAM_MAPPING = new HashMap<>();

    static {
        // 基础参数映射
        PARAM_MAPPING.put("temperature", "temperature");
        PARAM_MAPPING.put("top_p", "top_p");
        PARAM_MAPPING.put("top_k", "top_k");
        PARAM_MAPPING.put("max_tokens", "max_tokens");
        PARAM_MAPPING.put("stop", "stop");
        PARAM_MAPPING.put("stream", "stream");
        PARAM_MAPPING.put("seed", "seed");
        PARAM_MAPPING.put("frequency_penalty", "frequency_penalty");
        PARAM_MAPPING.put("presence_penalty", "presence_penalty");

        // 通义千问特有参数
        PARAM_MAPPING.put("tools", "tools");
        PARAM_MAPPING.put("tool_choice", "tool_choice");
        PARAM_MAPPING.put("result_format", "result_format");
        PARAM_MAPPING.put("enable_search", "enable_search");
        PARAM_MAPPING.put("incremental_output", "incremental_output");
    }

    @Override
    public Map<String, Object> mapParameters(Map<String, Object> genericParams) {
        Map<String, Object> tongyiParams = new HashMap<>();

        if (genericParams == null || genericParams.isEmpty()) {
            return tongyiParams;
        }

        // 处理模型名称
        processModelName(genericParams, tongyiParams);

        // 处理通用参数
        for (Map.Entry<String, Object> entry : genericParams.entrySet()) {
            String key = entry.getKey();
            String mappedKey = PARAM_MAPPING.get(key);

            if (mappedKey != null) {
                tongyiParams.put(mappedKey, entry.getValue());
            }
        }

        // 处理stop参数特殊情况
        processStopParam(genericParams, tongyiParams);

        // 处理响应格式
        processResponseFormat(genericParams, tongyiParams);

        return tongyiParams;
    }

    /**
     * 处理模型名称
     *
     * @param genericParams 通用参数
     * @param tongyiParams  通义千问参数
     */
    private void processModelName(Map<String, Object> genericParams, Map<String, Object> tongyiParams) {
        Object model = genericParams.get("model");
        if (model != null) {
            String modelStr = model.toString();

            // 如果模型名带有前缀（如tongyi:qwen-turbo），则截取真实模型名
            if (modelStr.contains(":")) {
                modelStr = modelStr.substring(modelStr.indexOf(":") + 1);
            }

            tongyiParams.put("model", modelStr);
        } else {
            // 默认模型
            tongyiParams.put("model", "qwen-turbo");
        }
    }

    /**
     * 处理停止词参数
     *
     * @param genericParams 通用参数
     * @param tongyiParams  通义千问参数
     */
    @SuppressWarnings("unchecked")
    private void processStopParam(Map<String, Object> genericParams, Map<String, Object> tongyiParams) {
        Object stopObj = genericParams.get("stop");
        if (stopObj != null) {
            if (stopObj instanceof String) {
                // 如果是单个字符串，则创建只包含一个元素的数组
                tongyiParams.put("stop", new String[]{(String) stopObj});
            } else if (stopObj instanceof List) {
                // 如果已经是列表，则直接使用
                tongyiParams.put("stop", ((List<String>) stopObj).toArray(new String[0]));
            } else if (stopObj instanceof String[]) {
                // 如果已经是数组，则直接使用
                tongyiParams.put("stop", stopObj);
            }
        }
    }

    /**
     * 处理响应格式
     *
     * @param genericParams 通用参数
     * @param tongyiParams  通义千问参数
     */
    private void processResponseFormat(Map<String, Object> genericParams, Map<String, Object> tongyiParams) {
        Object formatObj = genericParams.get("response_format");
        if (formatObj != null) {
            if (formatObj instanceof String) {
                String format = (String) formatObj;
                if ("json".equalsIgnoreCase(format)) {
                    // 通义千问的JSON格式参数
                    JSONObject resultFormat = new JSONObject();
                    resultFormat.put("type", "json");
                    tongyiParams.put("result_format", resultFormat);
                }
            } else if (formatObj instanceof Map) {
                // 如果已经是对象/映射，则转换为通义千问的格式
                Map<String, Object> formatMap = (Map<String, Object>) formatObj;
                if (formatMap.containsKey("type") && "json_object".equals(formatMap.get("type"))) {
                    JSONObject resultFormat = new JSONObject();
                    resultFormat.put("type", "json");
                    tongyiParams.put("result_format", resultFormat);
                }
            }
        }
    }


}
