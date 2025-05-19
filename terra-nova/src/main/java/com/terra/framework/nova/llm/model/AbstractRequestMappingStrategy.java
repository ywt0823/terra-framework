package com.terra.framework.nova.llm.model;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 抽象请求参数映射策略实现
 *
 * @author terra-nova
 */
public abstract class AbstractRequestMappingStrategy implements RequestMappingStrategy {

    /**
     * 参数映射表
     */
    protected final Map<String, String> paramMapping = new HashMap<>();

    /**
     * 构造函数，初始化通用参数映射
     */
    protected AbstractRequestMappingStrategy() {
        // 初始化通用参数映射
        initCommonParamMapping();
        // 初始化特定供应商参数映射
        initVendorSpecificParamMapping();
    }

    /**
     * 初始化通用参数映射
     */
    protected void initCommonParamMapping() {
        // 通用参数映射
        paramMapping.put("temperature", "temperature");
        paramMapping.put("top_p", "top_p");
        paramMapping.put("top_k", "top_k");
        paramMapping.put("frequency_penalty", "frequency_penalty");
        paramMapping.put("presence_penalty", "presence_penalty");
        paramMapping.put("max_tokens", "max_tokens");
        paramMapping.put("stop", "stop");
        paramMapping.put("stream", "stream");
        paramMapping.put("user", "user");
        paramMapping.put("seed", "seed");
    }

    /**
     * 初始化特定供应商参数映射，由子类实现
     */
    protected abstract void initVendorSpecificParamMapping();

    @Override
    public Map<String, Object> mapParameters(Map<String, Object> genericParams) {
        Map<String, Object> vendorParams = new HashMap<>();

        if (genericParams == null || genericParams.isEmpty()) {
            return vendorParams;
        }

        // 处理模型名称
        processModelName(genericParams, vendorParams);

        // 处理通用参数
        for (Map.Entry<String, Object> entry : genericParams.entrySet()) {
            String key = entry.getKey();
            String mappedKey = paramMapping.get(key);

            if (mappedKey != null) {
                vendorParams.put(mappedKey, entry.getValue());
            }
        }

        // 处理特殊参数
        processSpecialParameters(genericParams, vendorParams);

        return vendorParams;
    }

    /**
     * 处理模型名称
     *
     * @param genericParams 通用参数
     * @param vendorParams 供应商参数
     */
    protected void processModelName(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        Object model = genericParams.get("model");
        if (model != null) {
            String modelStr = model.toString();

            // 如果模型名带有前缀（如vendor:model），则截取真实模型名
            if (modelStr.contains(":")) {
                modelStr = modelStr.substring(modelStr.indexOf(":") + 1);
            }

            vendorParams.put("model", modelStr);
        } else {
            // 使用默认模型
            vendorParams.put("model", getDefaultModelName());
        }
    }

    /**
     * 处理停止词参数
     *
     * @param genericParams 通用参数
     * @param vendorParams 供应商参数
     */
    @SuppressWarnings("unchecked")
    protected void processStopParam(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        Object stopObj = genericParams.get("stop");
        if (stopObj != null) {
            if (stopObj instanceof String) {
                // 如果是单个字符串，则创建只包含一个元素的数组
                vendorParams.put("stop", new String[]{(String) stopObj});
            } else if (stopObj instanceof List) {
                // 如果已经是列表，则直接使用
                vendorParams.put("stop", ((List<String>) stopObj).toArray(new String[0]));
            } else if (stopObj instanceof String[]) {
                // 如果已经是数组，则直接使用
                vendorParams.put("stop", stopObj);
            }
        }
    }

    /**
     * 处理响应格式
     *
     * @param genericParams 通用参数
     * @param vendorParams 供应商参数
     */
    protected void processResponseFormat(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        Object formatObj = genericParams.get("response_format");
        if (formatObj != null) {
            if (formatObj instanceof String) {
                String format = (String) formatObj;
                if ("json".equalsIgnoreCase(format)) {
                    // 对于JSON格式，创建合适的格式对象
                    Map<String, String> responseFormat = new HashMap<>();
                    responseFormat.put("type", "json_object");
                    vendorParams.put("response_format", responseFormat);
                } else if ("text".equalsIgnoreCase(format)) {
                    Map<String, String> responseFormat = new HashMap<>();
                    responseFormat.put("type", "text");
                    vendorParams.put("response_format", responseFormat);
                }
            } else {
                // 如果已经是对象/映射，则直接使用
                vendorParams.put("response_format", formatObj);
            }
        }
    }

    /**
     * 处理特殊参数，由子类实现
     *
     * @param genericParams 通用参数
     * @param vendorParams 供应商参数
     */
    protected void processSpecialParameters(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        // 默认处理stop参数和响应格式
        processStopParam(genericParams, vendorParams);
        processResponseFormat(genericParams, vendorParams);
    }

    /**
     * 获取默认模型名称，由子类实现
     *
     * @return 默认模型名称
     */
    protected abstract String getDefaultModelName();
} 