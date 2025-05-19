package com.terra.framework.nova.llm.model.ollama;

import com.terra.framework.nova.llm.model.AbstractRequestMappingStrategy;
import java.util.Map;

/**
 * Ollama请求参数映射策略
 *
 * @author terra-nova
 */
public class OllamaRequestMappingStrategy extends AbstractRequestMappingStrategy {

    @Override
    protected void initVendorSpecificParamMapping() {
        // Ollama特有参数映射
        paramMapping.put("max_tokens", "num_predict");  // Ollama使用num_predict表示最大生成token数
        paramMapping.put("mirostat", "mirostat");
        paramMapping.put("mirostat_eta", "mirostat_eta");
        paramMapping.put("mirostat_tau", "mirostat_tau");
        paramMapping.put("num_ctx", "num_ctx");
        paramMapping.put("num_gpu", "num_gpu");
        paramMapping.put("num_thread", "num_thread");
        paramMapping.put("repeat_penalty", "repeat_penalty");
        paramMapping.put("tfs_z", "tfs_z");
        paramMapping.put("num_batch", "num_batch");
        paramMapping.put("numa", "numa");
        paramMapping.put("system", "system");  // 系统提示，在chat API中才有效
    }

    @Override
    protected void processSpecialParameters(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        // 调用父类的处理方法处理停止词等通用参数
        super.processSpecialParameters(genericParams, vendorParams);
        
        // 处理Ollama特有的API选择（generate vs chat）
        processApiChoice(genericParams, vendorParams);
    }

    /**
     * 处理API选择（generate vs chat）
     *
     * @param genericParams 通用参数
     * @param vendorParams Ollama参数
     */
    private void processApiChoice(Map<String, Object> genericParams, Map<String, Object> vendorParams) {
        // use_chat_api参数用于指示是否使用chat API而不是generate API
        Object useChatObj = genericParams.get("use_chat_api");
        if (useChatObj != null) {
            boolean useChat;
            if (useChatObj instanceof Boolean) {
                useChat = (Boolean) useChatObj;
            } else {
                useChat = Boolean.parseBoolean(useChatObj.toString());
            }

            // 保留此参数，以便适配器可以决定使用哪个API
            vendorParams.put("use_chat_api", useChat);
        } else {
            // 默认情况下，根据模型决定
            String model = vendorParams.getOrDefault("model", "llama2").toString();
            // 判断模型是否支持聊天接口
            boolean useChat = model.contains("chat") || model.contains("instruct");
            vendorParams.put("use_chat_api", useChat);
        }
    }

    @Override
    protected String getDefaultModelName() {
        return "llama2";
    }
}
