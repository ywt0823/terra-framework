package com.terra.framework.nova.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI a Terra a enabled.
 *
 * @author AI
 */
@Data
@ConfigurationProperties(prefix = "terra.ai")
public class TerraAiProperties {

    /**
     * Enable AI features. Defaults to false.
     */
    private boolean enabled = false;

    private String apiKey;

    private String apiUrl;

    private String model = "deepseek-chat"; // 如：deepseek-chat

}
