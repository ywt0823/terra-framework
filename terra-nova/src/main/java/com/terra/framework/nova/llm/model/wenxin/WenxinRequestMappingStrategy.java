package com.terra.framework.nova.llm.model.wenxin;

import com.terra.framework.nova.llm.model.AbstractRequestMappingStrategy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 文心一言请求参数映射策略
 *
 * @author terra-nova
 */
public class WenxinRequestMappingStrategy extends AbstractRequestMappingStrategy {

    @Override
    protected void initVendorSpecificParamMapping() {
        // 文心一言特有参数
        paramMapping.put("max_tokens", "max_output_tokens");  // 百度的最大输出长度参数名不同
        paramMapping.put("user", "user_id");  // 百度的用户ID参数名不同
        paramMapping.put("system", "system");
        paramMapping.put("penalty_score", "penalty_score");
        paramMapping.put("functions", "functions");
        paramMapping.put("disable_search", "disable_search");
        paramMapping.put("enable_citation", "enable_citation");
    }

    @Override
    protected void processSpecialParameters(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        // 调用父类的方法处理通用参数
        super.processSpecialParameters(genericParams, vendorParams);

        // 处理响应格式
        processResponseFormat(genericParams, vendorParams);
    }

    /**
     * 处理响应格式
     *
     * @param genericParams 通用参数
     * @param vendorParams 文心一言参数
     */
    protected void processResponseFormat(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
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
     * 获取模型名称（文心一言特有方法，供WenxinModel使用）
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
        return getDefaultModelName();
    }

    @Override
    protected String getDefaultModelName() {
        return "ernie-4.0";
    }
}
