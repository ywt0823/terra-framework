package com.terra.framework.nova.llm.core;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.exception.UnsupportedModelException;
import com.terra.framework.nova.llm.model.base.LLMModel;
import com.terra.framework.nova.llm.model.deepseek.DeepSeekModel;
import com.terra.framework.nova.llm.model.tongyi.TongyiModel;

/**
 * 模型工厂类
 */
public class ModelFactory {

    /**
     * 创建模型实例
     *
     * @param type   模型类型
     * @param config 模型配置
     * @param httpClientUtils HTTP客户端工具类
     * @return LLM模型实例
     * @throws UnsupportedModelException 不支持的模型类型
     */
    public static LLMModel createModel(ModelType type, ModelConfig config, HttpClientUtils httpClientUtils) {
        return switch (type) {
            case DEEPSEEK -> new DeepSeekModel(config, httpClientUtils);
            case TONGYI -> new TongyiModel(config, httpClientUtils);
        };
    }
}
