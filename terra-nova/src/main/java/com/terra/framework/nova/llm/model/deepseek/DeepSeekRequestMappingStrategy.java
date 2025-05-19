package com.terra.framework.nova.llm.model.deepseek;

import com.terra.framework.nova.llm.model.AbstractRequestMappingStrategy;

/**
 * DeepSeek请求参数映射策略
 *
 * @author terra-nova
 */
public class DeepSeekRequestMappingStrategy extends AbstractRequestMappingStrategy {

    @Override
    protected void initVendorSpecificParamMapping() {
        // DeepSeek特有参数
        paramMapping.put("tools", "tools");
        paramMapping.put("tool_choice", "tool_choice");
        paramMapping.put("safe_mode", "safe_mode");
    }

    @Override
    protected String getDefaultModelName() {
        return "deepseek-chat";
    }
}
