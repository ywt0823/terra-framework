package com.terra.framework.nova.model.claude;

import com.terra.framework.nova.model.RequestMappingStrategy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Claude请求参数映射策略
 *
 * @author terra-nova
 */
public class ClaudeRequestMappingStrategy implements RequestMappingStrategy {

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
        PARAM_MAPPING.put("stop", "stop_sequences");
        PARAM_MAPPING.put("stream", "stream");
        PARAM_MAPPING.put("user", "user");

        // Claude特有参数
        PARAM_MAPPING.put("system", "system"); // 系统提示
        PARAM_MAPPING.put("anthropic_version", "anthropic_version");
    }

    @Override
    public Map<String, Object> mapParameters(Map<String, Object> genericParams) {
        Map<String, Object> claudeParams = new HashMap<>();

        if (genericParams == null || genericParams.isEmpty()) {
            return claudeParams;
        }

        // 处理模型名称
        processModelName(genericParams, claudeParams);

        // 处理通用参数
        for (Map.Entry<String, Object> entry : genericParams.entrySet()) {
            String key = entry.getKey();
            String mappedKey = PARAM_MAPPING.get(key);

            if (mappedKey != null) {
                claudeParams.put(mappedKey, entry.getValue());
            }
        }

        // 处理停止序列特殊情况
        processStopSequences(genericParams, claudeParams);

        // Claude默认不需要设置响应格式，但可以设置特定的输出格式指令
        processResponseFormat(genericParams, claudeParams);

        return claudeParams;
    }

    /**
     * 处理模型名称
     *
     * @param genericParams 通用参数
     * @param claudeParams Claude参数
     */
    private void processModelName(Map<String, Object> genericParams, Map<String, Object> claudeParams) {
        Object model = genericParams.get("model");
        if (model != null) {
            String modelStr = model.toString();

            // 如果模型名带有前缀（如claude:claude-3-opus-20240229），则截取真实模型名
            if (modelStr.contains(":")) {
                modelStr = modelStr.substring(modelStr.indexOf(":") + 1);
            }

            claudeParams.put("model", modelStr);
        } else {
            // 默认模型
            claudeParams.put("model", "claude-3-opus-20240229");
        }
    }

    /**
     * 处理停止序列参数
     *
     * @param genericParams 通用参数
     * @param claudeParams Claude参数
     */
    @SuppressWarnings("unchecked")
    private void processStopSequences(Map<String, Object> genericParams, Map<String, Object> claudeParams) {
        Object stopObj = genericParams.get("stop");
        if (stopObj != null) {
            if (stopObj instanceof String) {
                // 如果是单个字符串，则创建只包含一个元素的数组
                claudeParams.put("stop_sequences", new String[]{(String) stopObj});
            } else if (stopObj instanceof List) {
                // 如果已经是列表，则直接使用
                claudeParams.put("stop_sequences", ((List<String>) stopObj).toArray(new String[0]));
            } else if (stopObj instanceof String[]) {
                // 如果已经是数组，则直接使用
                claudeParams.put("stop_sequences", stopObj);
            }
        }
    }

    /**
     * 处理响应格式
     *
     * @param genericParams 通用参数
     * @param claudeParams Claude参数
     */
    private void processResponseFormat(Map<String, Object> genericParams, Map<String, Object> claudeParams) {
        // Claude使用system参数或message content中的指令来控制输出格式
        Object formatObj = genericParams.get("response_format");
        if (formatObj != null) {
            if (formatObj instanceof String) {
                String format = (String) formatObj;
                if ("json".equalsIgnoreCase(format)) {
                    // 对于JSON格式，可以通过system参数添加指令
                    String systemPrompt = "请以有效的JSON格式返回您的响应。";
                    claudeParams.put("system", systemPrompt);
                }
            } else if (formatObj instanceof Map) {
                // 如果是复杂的响应格式对象，可以提取其中的类型
                Map<String, Object> formatMap = (Map<String, Object>) formatObj;
                if (formatMap.containsKey("type") && "json_object".equals(formatMap.get("type"))) {
                    String systemPrompt = "请以有效的JSON格式返回您的响应。";
                    claudeParams.put("system", systemPrompt);
                }
            }
        }
    }
}
