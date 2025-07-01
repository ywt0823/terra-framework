package com.terra.framework.autoconfigure.nova.properties;

import lombok.Data;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.OpenAiEmbeddingOptions;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI a Terra a enabled.
 *
 * @author AI
 */
@Data
@ConfigurationProperties(prefix = "terra.ai")
@ConditionalOnClass({OpenAiChatOptions.class, OpenAiEmbeddingOptions.class})
public class TerraAiProperties {

    /**
     * Enable AI features. Defaults to false.
     */
    private boolean enabled = false;

    private DeepSeekProperties deepseek = new DeepSeekProperties();
    private VectorStoreProperties vectorStore = new VectorStoreProperties();
    private MemoryProperties memory = new MemoryProperties();

    @Data
    public static class DeepSeekProperties {
        private boolean enabled = false;
        private String baseUrl = "https://api.deepseek.com/v1";
        private String apiKey;
        private ChatProperties chat = new ChatProperties();
        private EmbeddingProperties embedding = new EmbeddingProperties();
    }

    @Data
    public static class ChatProperties {
        private boolean enabled = true;
        private OpenAiChatOptions options = OpenAiChatOptions.builder()
            .withModel("deepseek-chat")
            .withTemperature(0.7f)
            .build();
    }

    @Data
    public static class EmbeddingProperties {
        private boolean enabled = true;
        private OpenAiEmbeddingOptions options = OpenAiEmbeddingOptions.builder()
            .withModel("deepseek-text-embedding-v2")
            .build();
    }

    @Data
    public static class VectorStoreProperties {
        /**
         * The type of vector store to use.
         * Supported values: 'in-memory', 'redis'.
         */
        private String type = "in-memory";
    }

    @Data
    public static class MemoryProperties {
        /**
         * The type of conversation memory to use.
         * Supported values: 'in-memory'.
         */
        private String type = "in-memory";
        /**
         * The maximum number of recent conversation exchanges to keep.
         */
        private int maxHistory = 10;
    }
}
