package com.terra.framework.nova.llm.model.openai;

import com.terra.framework.nova.llm.model.AbstractRequestMappingStrategy;

/**
 * OpenAI请求参数映射策略
 *
 * @author terra-nova
 */
public class OpenAIRequestMappingStrategy extends AbstractRequestMappingStrategy {

    @Override
    protected void initVendorSpecificParamMapping() {
        // OpenAI特有参数
        paramMapping.put("response_format", "response_format");
        paramMapping.put("functions", "functions");
        paramMapping.put("tools", "tools");
        paramMapping.put("tool_choice", "tool_choice");
        paramMapping.put("logit_bias", "logit_bias");
    }

    @Override
    protected String getDefaultModelName() {
        return "gpt-3.5-turbo";
    }
}
