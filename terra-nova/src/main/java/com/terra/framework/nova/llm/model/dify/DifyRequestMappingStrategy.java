package com.terra.framework.nova.llm.model.dify;

import com.terra.framework.nova.llm.model.RequestMappingStrategy;
import java.util.HashMap;
import java.util.Map;

/**
 * Dify请求参数映射策略
 *
 * @author terra-nova
 */
public class DifyRequestMappingStrategy implements RequestMappingStrategy {

    /**
     * 参数映射表
     */
    private static final Map<String, String> PARAM_MAPPING = new HashMap<>();

    static {
        // 基础参数映射
        PARAM_MAPPING.put("temperature", "temperature");
        PARAM_MAPPING.put("top_p", "top_p");
        PARAM_MAPPING.put("top_k", "top_k");
        PARAM_MAPPING.put("stream", "stream");
        PARAM_MAPPING.put("user", "user");

        // Dify特有参数
        PARAM_MAPPING.put("conversation_id", "conversation_id");
        PARAM_MAPPING.put("files", "files");
        PARAM_MAPPING.put("response_mode", "response_mode");
    }

    @Override
    public Map<String, Object> mapParameters(Map<String, Object> genericParams) {
        Map<String, Object> difyParams = new HashMap<>();

        if (genericParams == null || genericParams.isEmpty()) {
            return difyParams;
        }

        // Dify API不需要在请求中指定模型，忽略model参数

        // 处理通用参数
        for (Map.Entry<String, Object> entry : genericParams.entrySet()) {
            String key = entry.getKey();
            String mappedKey = PARAM_MAPPING.get(key);

            if (mappedKey != null) {
                difyParams.put(mappedKey, entry.getValue());
            }
        }

        // 处理应用程序ID
        processAppId(genericParams, difyParams);

        return difyParams;
    }

    /**
     * 处理应用程序ID
     *
     * @param genericParams 通用参数
     * @param difyParams Dify参数
     */
    private void processAppId(Map<String, Object> genericParams, Map<String, Object> difyParams) {
        // 从model参数中提取应用程序ID
        Object modelObj = genericParams.get("model");
        if (modelObj != null && modelObj.toString().contains(":")) {
            String modelStr = modelObj.toString();
            String appId = modelStr.substring(modelStr.indexOf(":") + 1);

            // 应用程序ID实际通过URL路径传递，而不是请求参数
            // 这里临时保存以供Model类使用
            difyParams.put("_app_id", appId);
        }
    }

    /**
     * 获取Dify应用程序ID
     *
     * @param parameters 参数映射
     * @return 应用程序ID
     */
    public String getAppId(Map<String, Object> parameters) {
        // 尝试从已映射的参数中获取
        Object appIdObj = parameters.get("_app_id");
        if (appIdObj != null) {
            return appIdObj.toString();
        }

        // 尝试从model参数中提取
        Object modelObj = parameters.get("model");
        if (modelObj != null) {
            String modelStr = modelObj.toString();
            if (modelStr.contains(":")) {
                return modelStr.substring(modelStr.indexOf(":") + 1);
            }
            return modelStr;
        }

        // 如果无法确定，返回null
        return null;
    }
}
