package com.terra.framework.nova.core.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Bean;
import org.springframework.ai.openai.OpenAiApi;
import org.springframework.ai.openai.OpenAiChatClient;
import org.springframework.ai.openai.api.OpenAiApi.OpenAiApiBuilder;

@Configuration
@ComponentScan("com.terra.framework.nova")
@EnableConfigurationProperties(NovaProperties.class)
public class NovaConfiguration {
    
    @Bean
    public OpenAiApi openAiApi(NovaProperties properties) {
        return OpenAiApi.builder()
            .withApiKey(getOpenAiApiKey())
            .withEndpoint(properties.getGpt().getProviders().getOrDefault("openai.endpoint", 
                "https://api.openai.com/v1").toString())
            .build();
    }

    @Bean
    public OpenAiChatClient openAiChatClient(OpenAiApi openAiApi) {
        return new OpenAiChatClient(openAiApi);
    }

    private String getOpenAiApiKey() {
        String apiKey = System.getenv("OPENAI_API_KEY");
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalStateException("OPENAI_API_KEY environment variable is not set");
        }
        return apiKey;
    }
} 