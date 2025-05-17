package com.terra.framework.nova.model.wenxin;

import com.terra.framework.nova.model.RequestMappingStrategy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文心一言请求参数映射策略
 *
 * @author terra-nova
 */
public class WenxinRequestMappingStrategy implements RequestMappingStrategy {

    /**
     * 参数映射表
     */
    private static final Map<String, String> PARAM_MAPPING = new HashMap<>();

    static {
        // 基础参数映射
        PARAM_MAPPING.put("temperature", "temperature");
        PARAM_MAPPING.put("top_p", "top_p");
        PARAM_MAPPING.put("top_k", "top_k");
        PARAM_MAPPING.put("max_tokens", "max_output_tokens");  // 百度的最大输出长度参数名不同
        PARAM_MAPPING.put("stop", "stop");
        PARAM_MAPPING.put("stream", "stream");
        PARAM_MAPPING.put("user", "user_id");  // 百度的用户ID参数名不同

        // 文心一言特有参数
        PARAM_MAPPING.put("system", "system");
        PARAM_MAPPING.put("penalty_score", "penalty_score");
        PARAM_MAPPING.put("functions", "functions");
        PARAM_MAPPING.put("disable_search", "disable_search");
        PARAM_MAPPING.put("enable_citation", "enable_citation");
    }

    @Override
    public Map<String, Object> mapParameters(Map<String, Object> genericParams) {
        Map<String, Object> wenxinParams = new HashMap<>();

        if (genericParams == null || genericParams.isEmpty()) {
            return wenxinParams;
        }

        // 文心一言不通过请求参数指定模型，而是通过URL指定

        // 处理通用参数
        for (Map.Entry<String, Object> entry : genericParams.entrySet()) {
            String key = entry.getKey();
            String mappedKey = PARAM_MAPPING.get(key);

            if (mappedKey != null) {
                wenxinParams.put(mappedKey, entry.getValue());
            }
        }

        // 处理stop参数特殊情况
        processStopParam(genericParams, wenxinParams);

        // 处理响应格式
        processResponseFormat(genericParams, wenxinParams);

        return wenxinParams;
    }

    /**
     * 处理停止词参数
     *
     * @param genericParams 通用参数
     * @param wenxinParams 文心一言参数
     */
    @SuppressWarnings("unchecked")
    private void processStopParam(Map<String, Object> genericParams, Map<String, Object> wenxinParams) {
        Object stopObj = genericParams.get("stop");
        if (stopObj != null) {
            if (stopObj instanceof String) {
                // 如果是单个字符串，则创建只包含一个元素的数组
                wenxinParams.put("stop", new String[]{(String) stopObj});
            } else if (stopObj instanceof List) {
                // 如果已经是列表，则直接使用
                wenxinParams.put("stop", ((List<String>) stopObj).toArray(new String[0]));
            } else if (stopObj instanceof String[]) {
                // 如果已经是数组，则直接使用
                wenxinParams.put("stop", stopObj);
            }
        }
    }

    /**
     * 处理响应格式
     *
     * @param genericParams 通用参数
     * @param wenxinParams 文心一言参数
     */
    private void processResponseFormat(Map<String, Object> genericParams, Map<String, Object> wenxinParams) {
        Object formatObj = genericParams.get("response_format");
        if (formatObj != null) {
            if (formatObj instanceof String) {
                String format = (String) formatObj;
                // 文心一言暂不支持直接设置响应格式，可以考虑通过指令来实现
                if ("json".equalsIgnoreCase(format)) {
                    // 可以通过user消息中添加要求JSON返回的指令
                    // 这里不直接设置参数，而是在消息处理时考虑
                }
            }
        }
    }

    /**
     * 获取模型名称
     *
     * @param genericParams 通用参数
     * @return 模型名称（用于构建URL）
     */
    public String getModelName(Map<String, Object> genericParams) {
        Object modelObj = genericParams.get("model");
        if (modelObj != null) {
            String modelStr = modelObj.toString();

            // 如果模型名带有前缀（如wenxin:ernie-4.0），则截取真实模型名
            if (modelStr.contains(":")) {
                modelStr = modelStr.substring(modelStr.indexOf(":") + 1);
            }

            return modelStr;
        }

        // 默认模型
        return "ernie-4.0";
    }
}
