package com.terra.framework.nova.llm.model.claude;

import com.terra.framework.nova.llm.model.AbstractRequestMappingStrategy;
import java.util.List;
import java.util.Map;

/**
 * Claude请求参数映射策略
 *
 * @author terra-nova
 */
public class ClaudeRequestMappingStrategy extends AbstractRequestMappingStrategy {

    @Override
    protected void initVendorSpecificParamMapping() {
        // Claude特有参数映射
        paramMapping.put("stop", "stop_sequences"); // Claude使用stop_sequences而非stop
        paramMapping.put("system", "system"); // 系统提示
        paramMapping.put("anthropic_version", "anthropic_version");
    }

    @Override
    protected void processSpecialParameters(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        // 处理Claude特有的参数逻辑
        processStopParam(genericParams, vendorParams); // 重写父类方法中对stop参数的处理
        processClaudeResponseFormat(genericParams, vendorParams);
    }

    /**
     * 处理响应格式
     *
     * @param genericParams 通用参数
     * @param vendorParams Claude参数
     */
    private void processClaudeResponseFormat(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        // Claude使用system参数或message content中的指令来控制输出格式
        Object formatObj = genericParams.get("response_format");
        if (formatObj != null) {
            if (formatObj instanceof String) {
                String format = (String) formatObj;
                if ("json".equalsIgnoreCase(format)) {
                    // 对于JSON格式，可以通过system参数添加指令
                    String systemPrompt = "请以有效的JSON格式返回您的响应。";
                    vendorParams.put("system", systemPrompt);
                }
            } else if (formatObj instanceof Map) {
                // 如果是复杂的响应格式对象，可以提取其中的类型
                Map<String, Object> formatMap = (Map<String, Object>) formatObj;
                if (formatMap.containsKey("type") && "json_object".equals(formatMap.get("type"))) {
                    String systemPrompt = "请以有效的JSON格式返回您的响应。";
                    vendorParams.put("system", systemPrompt);
                }
            }
        }
    }

    @Override
    protected void processStopParam(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        Object stopObj = genericParams.get("stop");
        if (stopObj != null) {
            if (stopObj instanceof String) {
                // 如果是单个字符串，则创建只包含一个元素的数组
                vendorParams.put("stop_sequences", new String[]{(String) stopObj});
            } else if (stopObj instanceof List) {
                // 如果已经是列表，则直接使用
                vendorParams.put("stop_sequences", ((List<String>) stopObj).toArray(new String[0]));
            } else if (stopObj instanceof String[]) {
                // 如果已经是数组，则直接使用
                vendorParams.put("stop_sequences", stopObj);
            }
        }
    }

    @Override
    protected String getDefaultModelName() {
        return "claude-3-opus-20240229";
    }
}
