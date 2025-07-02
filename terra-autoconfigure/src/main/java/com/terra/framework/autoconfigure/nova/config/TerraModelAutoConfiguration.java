package com.terra.framework.autoconfigure.nova.config;

import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiAutoConfiguration;
import org.springframework.ai.autoconfigure.openai.OpenAiChatProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiConnectionProperties;
import org.springframework.ai.autoconfigure.openai.OpenAiEmbeddingProperties;
import org.springframework.ai.document.MetadataMode;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnProperty(prefix = "terra.ai.deepseek", name = "enabled", havingValue = "true")
@ConditionalOnClass({OpenAiConnectionProperties.class, OpenAiChatProperties.class, OpenAiEmbeddingProperties.class})
@AutoConfigureBefore(OpenAiAutoConfiguration.class)
public class TerraModelAutoConfiguration {


    @Bean
    @Primary
    public OpenAiConnectionProperties openAiConnectionProperties(TerraAiProperties terraAiProperties) {
        OpenAiConnectionProperties openAiConnectionProperties = new OpenAiConnectionProperties();
        openAiConnectionProperties.setApiKey(terraAiProperties.getDeepseek().getApiKey());
        openAiConnectionProperties.setBaseUrl(terraAiProperties.getDeepseek().getBaseUrl());
        return openAiConnectionProperties;
    }

    @Bean
    @Primary
    public OpenAiChatProperties openAiChatProperties(OpenAiConnectionProperties openAiConnectionProperties,
                                                     TerraAiProperties terraAiProperties) {
        OpenAiChatProperties openAiChatProperties = new OpenAiChatProperties();
        openAiChatProperties.setOptions(terraAiProperties.getDeepseek().getChat().getOptions());
        openAiChatProperties.setBaseUrl(openAiConnectionProperties.getBaseUrl());
        openAiChatProperties.setApiKey(openAiConnectionProperties.getApiKey());
        return openAiChatProperties;
    }

    @Bean
    @Primary
    public OpenAiEmbeddingProperties openAiEmbeddingProperties(OpenAiConnectionProperties openAiConnectionProperties,
                                                               TerraAiProperties terraAiProperties) {
        OpenAiEmbeddingProperties openAiEmbeddingProperties = new OpenAiEmbeddingProperties();
        openAiEmbeddingProperties.setBaseUrl(openAiConnectionProperties.getBaseUrl());
        openAiEmbeddingProperties.setApiKey(openAiConnectionProperties.getApiKey());
        openAiEmbeddingProperties.setOptions(terraAiProperties.getDeepseek().getEmbedding().getOptions());
        openAiEmbeddingProperties.setMetadataMode(MetadataMode.EMBED);
        return openAiEmbeddingProperties;
    }
}
