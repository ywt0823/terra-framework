package com.terra.framework.autoconfigure.nova.config;

import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.document.MetadataMode;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiEmbeddingModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.util.Assert;

@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnProperty(prefix = "terra.ai.deepseek", name = "enabled", havingValue = "true")
@ConditionalOnClass(TerraAiProperties.class)
public class DeepSeekAutoConfiguration {

    private final TerraAiProperties properties;

    public DeepSeekAutoConfiguration(TerraAiProperties properties) {
        this.properties = properties;
    }

    @Bean
    @ConditionalOnClass(ChatModel.class)
    @ConditionalOnProperty(prefix = "terra.ai.deepseek.chat", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ChatModel deepSeekChatModel() {
        TerraAiProperties.DeepSeekProperties deepSeek = properties.getDeepseek();
        Assert.hasText(deepSeek.getApiKey(), "terra.ai.deepseek.api-key must be set");

        OpenAiApi openAiApi = new OpenAiApi(deepSeek.getBaseUrl(), deepSeek.getApiKey());

        return new OpenAiChatModel(openAiApi, deepSeek.getChat().getOptions());
    }

    @Bean
    @ConditionalOnClass(EmbeddingModel.class)
    @ConditionalOnProperty(prefix = "terra.ai.deepseek.embedding", name = "enabled", havingValue = "true", matchIfMissing = true)
    public EmbeddingModel deepSeekEmbeddingModel() {
        TerraAiProperties.DeepSeekProperties deepSeek = properties.getDeepseek();
        Assert.hasText(deepSeek.getApiKey(), "terra.ai.deepseek.api-key must be set");

        OpenAiApi openAiApi = new OpenAiApi(deepSeek.getBaseUrl(), deepSeek.getApiKey());

        return new OpenAiEmbeddingModel(openAiApi, MetadataMode.EMBED, deepSeek.getEmbedding().getOptions(), new RetryTemplate());
    }
}
