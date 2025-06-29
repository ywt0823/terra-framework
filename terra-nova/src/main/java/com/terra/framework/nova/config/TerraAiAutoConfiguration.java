package com.terra.framework.nova.config;

import com.terra.framework.nova.properties.TerraAiProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Import;

/**
 * @author AI
 */
@ConditionalOnProperty(prefix = "terra.ai", name = "enabled", havingValue = "true")
@EnableConfigurationProperties(TerraAiProperties.class)
@Import({
    DeepSeekAutoConfiguration.class,
    VectorStoreAutoConfiguration.class,
    ConversationMemoryAutoConfiguration.class
})
public class TerraAiAutoConfiguration {

}
