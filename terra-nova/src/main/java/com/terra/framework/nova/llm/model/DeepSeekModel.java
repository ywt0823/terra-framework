package com.terra.framework.nova.llm.model;

import com.terra.framework.nova.llm.core.LLMModel;
import com.terra.framework.nova.llm.core.ModelConfig;
import dev.langchain4j.model.chat.ChatLanguageModel;
import dev.langchain4j.model.ollama.OllamaChatModel;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.Flow.Publisher;

/**
 * DeepSeek模型适配器
 */
@Slf4j
public class DeepSeekModel implements LLMModel {

    private final ModelConfig config;
    private ChatLanguageModel model;

    public DeepSeekModel(ModelConfig config) {
        this.config = config;
    }

    @Override
    public void init() {
        log.info("Initializing DeepSeek model with endpoint: {}", config.getApiEndpoint());
        this.model = OllamaChatModel.builder()
            .baseUrl(config.getApiEndpoint())
            .modelName("deepseek-r1")
            .temperature(config.getTemperature())
            .maxRetries(config.getMaxTokens())
            .build();
    }

    @Override
    public String predict(String prompt) {
        log.debug("Sending prediction request to DeepSeek model with prompt: {}", prompt);
        return model.generate(prompt);
    }

    @Override
    public Publisher<String> predictStream(String prompt) {
        // TODO: 实现流式预测
        throw new UnsupportedOperationException("Stream prediction not implemented yet");
    }

    @Override
    public void close() {
        log.info("Closing DeepSeek model");
        // 清理资源
    }
}
