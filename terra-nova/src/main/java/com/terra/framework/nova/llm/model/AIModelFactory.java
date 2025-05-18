package com.terra.framework.nova.llm.model;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.llm.model.claude.ClaudeModel;
import com.terra.framework.nova.llm.model.deepseek.DeepSeekModel;
import com.terra.framework.nova.llm.model.dify.DifyModel;
import com.terra.framework.nova.llm.model.ollama.OllamaModel;
import com.terra.framework.nova.llm.model.openai.OpenAIModel;
import com.terra.framework.nova.llm.model.tongyi.TongyiModel;
import com.terra.framework.nova.llm.model.wenxin.WenxinModel;
import lombok.extern.slf4j.Slf4j;

/**
 * AI模型工厂
 *
 * @author terra-nova
 */
@Slf4j
public class AIModelFactory {

    /**
     * 创建模型实例
     *
     * @param config     模型配置
     * @param httpClient HTTP客户端
     * @return AI模型实例
     */
    public static AIModel createModel(ModelConfig config, HttpClientUtils httpClient) {
        if (config == null) {
            throw new IllegalArgumentException("模型配置不能为空");
        }

        if (httpClient == null) {
            throw new IllegalArgumentException("HTTP客户端不能为空");
        }

        log.info("创建模型实例: {}, 类型: {}", config.getModelId(), config.getModelType());

        return switch (config.getModelType()) {
            case OPENAI -> new OpenAIModel(config, httpClient);
            case CLAUDE -> new ClaudeModel(config, httpClient);
            case WENXIN -> new WenxinModel(config, httpClient);
            case TONGYI -> new TongyiModel(config, httpClient);
            case DEEPSEEK -> new DeepSeekModel(config, httpClient);
            case DIFY -> new DifyModel(config, httpClient);
            case OLLAMA -> new OllamaModel(config, httpClient);
        };
    }
}
