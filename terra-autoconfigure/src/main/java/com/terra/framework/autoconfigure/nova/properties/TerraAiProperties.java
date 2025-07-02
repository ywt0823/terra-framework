package com.terra.framework.autoconfigure.nova.properties;

import lombok.Data;
import org.springframework.ai.model.SpringAIModels;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * AI a Terra a enabled.
 *
 * @author AI
 */
@Data
@ConfigurationProperties(prefix = "spring.ai.model")
public class TerraAiProperties {

    private SpringAIModels modelType;


}
