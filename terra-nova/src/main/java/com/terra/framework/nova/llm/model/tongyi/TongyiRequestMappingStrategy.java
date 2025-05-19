package com.terra.framework.nova.llm.model.tongyi;

import com.alibaba.fastjson.JSONObject;
import com.terra.framework.nova.llm.model.AbstractRequestMappingStrategy;
import java.util.Map;

/**
 * 通义千问请求参数映射策略
 *
 * @author terra-nova
 */
public class TongyiRequestMappingStrategy extends AbstractRequestMappingStrategy {

    @Override
    protected void initVendorSpecificParamMapping() {
        // 通义千问特有参数
        paramMapping.put("tools", "tools");
        paramMapping.put("tool_choice", "tool_choice");
        paramMapping.put("result_format", "result_format");
        paramMapping.put("enable_search", "enable_search");
        paramMapping.put("incremental_output", "incremental_output");
    }

    @Override
    protected void processSpecialParameters(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        // 调用父类的处理方法
        super.processSpecialParameters(genericParams, vendorParams);
        
        // 处理通义千问特有的响应格式
        processTongyiResponseFormat(genericParams, vendorParams);
    }

    /**
     * 处理通义千问特有的响应格式
     *
     * @param genericParams 通用参数
     * @param vendorParams  通义千问参数
     */
    private void processTongyiResponseFormat(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        Object formatObj = genericParams.get("response_format");
        if (formatObj != null) {
            if (formatObj instanceof String) {
                String format = (String) formatObj;
                if ("json".equalsIgnoreCase(format)) {
                    // 通义千问的JSON格式参数
                    JSONObject resultFormat = new JSONObject();
                    resultFormat.put("type", "json");
                    vendorParams.put("result_format", resultFormat);
                }
            } else if (formatObj instanceof Map) {
                // 如果已经是对象/映射，则转换为通义千问的格式
                Map<String, Object> formatMap = (Map<String, Object>) formatObj;
                if (formatMap.containsKey("type") && "json_object".equals(formatMap.get("type"))) {
                    JSONObject resultFormat = new JSONObject();
                    resultFormat.put("type", "json");
                    vendorParams.put("result_format", resultFormat);
                }
            }
        }
    }

    @Override
    protected String getDefaultModelName() {
        return "qwen-turbo";
    }
}
