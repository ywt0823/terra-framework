package com.terra.framework.nova.llm.model.dify;

import com.terra.framework.nova.llm.model.AbstractRequestMappingStrategy;
import java.util.HashMap;
import java.util.Map;

/**
 * Dify请求参数映射策略
 *
 * @author terra-nova
 */
public class DifyRequestMappingStrategy extends AbstractRequestMappingStrategy {

    @Override
    protected void initVendorSpecificParamMapping() {
        // Dify特有参数
        paramMapping.put("conversation_id", "conversation_id");
        paramMapping.put("files", "files");
        paramMapping.put("response_mode", "response_mode");
    }

    @Override
    protected void processSpecialParameters(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        // 调用父类处理通用特殊参数
        super.processSpecialParameters(genericParams, vendorParams);
        
        // 处理应用程序ID
        processAppId(genericParams, vendorParams);
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

    @Override
    protected String getDefaultModelName() {
        return "default";
    }
}
