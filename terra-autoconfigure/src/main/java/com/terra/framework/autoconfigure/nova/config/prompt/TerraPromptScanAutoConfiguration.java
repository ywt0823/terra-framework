package com.terra.framework.autoconfigure.nova.config.prompt;

import com.terra.framework.autoconfigure.nova.config.deepseek.TerraDeepSeekAutoConfiguration;
import com.terra.framework.autoconfigure.nova.config.openai.TerraOpenaiAutoConfiguration;
import com.terra.framework.autoconfigure.nova.registrar.PromptMapperRegistrar;
import com.terra.framework.nova.prompt.template.PromptTemplateRegistry;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.util.StringUtils;

/**
 * @author Zeus
 * @date 2025年07月15日 09:19
 * @description TerraPromptScanAutoConfiguration
 */
@ConditionalOnProperty(prefix = "terra.nova.prompt", name = "auto-scan", havingValue = "true", matchIfMissing = true)
@ConditionalOnMissingBean(name = "promptMapperScanRegistrar")
@Import(PromptMapperRegistrar.class)
@ConditionalOnBean(ChatModel.class)
@EnableConfigurationProperties(TerraPromptProperties.class)
@AutoConfigureAfter({TerraDeepSeekAutoConfiguration.class, TerraOpenaiAutoConfiguration.class})
public class TerraPromptScanAutoConfiguration {

    /**
     * 为传统扫描模式配置默认的 ChatModel（如果指定了的话）
     */
    @Bean
    @ConditionalOnProperty(prefix = "terra.nova.prompt", name = "default-chat-model")
    @ConditionalOnMissingBean(name = "defaultPromptMapperChatModel")
    public ChatModel defaultPromptMapperChatModel(TerraPromptProperties properties, ApplicationContext context) {
        String defaultChatModel = properties.getDefaultChatModel();
        if (StringUtils.hasText(defaultChatModel)) {
            return context.getBean(defaultChatModel, ChatModel.class);
        }
        return context.getBean(ChatModel.class);
    }

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
