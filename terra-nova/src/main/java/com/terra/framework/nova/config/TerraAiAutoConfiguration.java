package com.terra.framework.nova.config;

import com.terra.framework.nova.properties.TerraAiProperties;
import com.terra.framework.nova.service.AiService;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.ai.vectorstore.SimpleVectorStore;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author AI
 */
@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnProperty(prefix = "terra.ai", name = "enabled", havingValue = "true")
public class TerraAiAutoConfiguration {


    @Bean
    public ChatClient terraNovaChatClient(TerraAiProperties terraAiProperties) {
        OpenAiApi openAiApi = new OpenAiApi(terraAiProperties.getApiUrl(), terraAiProperties.getApiKey());
        OpenAiChatModel chatModel = new OpenAiChatModel(openAiApi);
        return ChatClient.builder(chatModel).build();
    }


    @Bean
    @ConditionalOnMissingBean
    public VectorStore vectorStore(EmbeddingModel embeddingModel) {
        return new SimpleVectorStore(embeddingModel);
    }

    @Bean
    public AiService aiService(VectorStore vectorStore, ChatClient chatClient) {
        return new AiService(chatClient, vectorStore);
    }

}
