package com.terra.framework.autoconfigure.nova.config.prompt;

import com.terra.framework.autoconfigure.nova.config.deepseek.TerraDeepSeekAutoConfiguration;
import com.terra.framework.autoconfigure.nova.config.openai.TerraOpenaiAutoConfiguration;
import com.terra.framework.autoconfigure.nova.properties.TerraPromptProperties;
import com.terra.framework.nova.prompt.registrar.PromptMapperRegistrar;
import com.terra.framework.nova.prompt.template.PromptTemplateRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

/**
 * @author Zeus
 * @date 2025年07月15日 09:19
 * @description TerraPromptScanAutoConfiguration
 */
@Import(PromptMapperRegistrar.class)
@ConditionalOnBean(ChatModel.class)
@EnableConfigurationProperties(TerraPromptProperties.class)
@AutoConfigureAfter({TerraDeepSeekAutoConfiguration.class, TerraOpenaiAutoConfiguration.class})
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
