package com.terra.framework.autoconfigure.nova.config.deepseek;

import com.terra.framework.autoconfigure.nova.annoation.ConditionalOnModelEnabled;
import com.terra.framework.autoconfigure.nova.properties.TerraAiProperties;
import com.terra.framework.nova.client.deepseek.DeepSeekChatClient;
import org.springframework.ai.deepseek.DeepSeekChatModel;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatAutoConfiguration;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekChatProperties;
import org.springframework.ai.model.deepseek.autoconfigure.DeepSeekConnectionProperties;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import static org.springframework.ai.model.SpringAIModels.DEEPSEEK;

/**
 * Terra DeepSeek 自动配置类。
 * <p>
 * 提供 DeepSeek 模型相关的 Bean 配置，包括 DeepSeekChatClient 等。
 *
 * @author <a href="mailto:love.yu@terra.com">Yu</a>
 * @version 1.0.0
 * @since 1.0.0
 */
@EnableConfigurationProperties(TerraAiProperties.class)
@ConditionalOnClass({DeepSeekConnectionProperties.class, DeepSeekChatProperties.class})
@AutoConfigureAfter({DeepSeekChatAutoConfiguration.class})
@ConditionalOnModelEnabled(DEEPSEEK)
public class TerraDeepSeekClientAutoConfiguration {

    /**
     * 创建 DeepSeekChatClient Bean。
     * <p>
     * 当 DeepSeekChatModel Bean 存在时，自动创建 DeepSeekChatClient 实例。
     *
     * @param deepSeekChatModel DeepSeek 聊天模型实例
     * @return DeepSeekChatClient 实例
     */
    @Bean
    @ConditionalOnBean(DeepSeekChatModel.class)
    public DeepSeekChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel) {
        return new DeepSeekChatClient(deepSeekChatModel);
    }
}
