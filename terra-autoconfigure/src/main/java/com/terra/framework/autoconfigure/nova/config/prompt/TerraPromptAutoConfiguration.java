package com.terra.framework.autoconfigure.nova.config.prompt;

import com.terra.framework.nova.prompt.registrar.PromptMapperRegistrar;
import com.terra.framework.nova.prompt.template.PromptTemplateRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;

/**
 * Autoconfiguration for Terra Prompt Mapper functionality.
 * <p>
 * This configuration automatically sets up the prompt template registry and
 * enables scanning for @PromptMapper interfaces when a ChatModel is available.
 *
 * @author DeavyJones
 */
@Configuration
@ConditionalOnBean(ChatModel.class)
@EnableConfigurationProperties(TerraPromptProperties.class)
@Import(PromptMapperRegistrar.class)
public class TerraPromptAutoConfiguration {

    /**
     * Creates a PromptTemplateRegistry bean that loads prompt templates from configured locations.
     *
     * @param properties The prompt configuration properties
     * @return A configured PromptTemplateRegistry
     */
    @Bean
    @ConditionalOnMissingBean
    public PromptTemplateRegistry promptTemplateRegistry(TerraPromptProperties properties) {
        PromptTemplateRegistry registry = new PromptTemplateRegistry();
        registry.loadTemplates(properties.getMapperLocations());
        return registry;
    }
}
