package com.terra.framework.nova.model.config;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import com.terra.framework.nova.model.client.ModelClient;
import com.terra.framework.nova.model.client.OllamaClient;
import com.terra.framework.nova.model.client.OpenAIClient;
import com.terra.framework.nova.model.properties.ModelProperties;
import com.terra.framework.nova.model.router.DefaultModelRouter;
import com.terra.framework.nova.model.router.ModelRouter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 模型自动配置类
 *
 * @author terra-nova
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(ModelProperties.class)
@ConditionalOnProperty(prefix = "terra.nova.model", name = "enabled", havingValue = "true", matchIfMissing = true)
public class ModelAutoConfiguration {

    /**
     * 配置OpenAI客户端
     */
    @Bean
    @ConditionalOnProperty(prefix = "terra.nova.model.openai", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ModelClient openAIClient(ModelProperties properties, HttpClientUtils httpClientUtils) {
        log.info("正在配置OpenAI客户端");
        String apiKey = properties.getOpenai().getApiKey();
        if (apiKey == null || apiKey.trim().isEmpty()) {
            log.warn("未配置OpenAI API密钥，将尝试从环境变量获取");
            apiKey = System.getenv("OPENAI_API_KEY");
            if (apiKey == null || apiKey.trim().isEmpty()) {
                log.error("无法获取OpenAI API密钥，OpenAI客户端将无法正常工作");
            }
        }
        return new OpenAIClient(httpClientUtils, apiKey);
    }

    /**
     * 配置Ollama客户端
     */
    @Bean
    @ConditionalOnProperty(prefix = "terra.nova.model.ollama", name = "enabled", havingValue = "true", matchIfMissing = true)
    public ModelClient ollamaClient(ModelProperties properties, HttpClientUtils httpClientUtils) {
        log.info("正在配置Ollama客户端");
        String baseUrl = properties.getOllama().getBaseUrl();
        return new OllamaClient(httpClientUtils, baseUrl);
    }

    /**
     * 配置模型路由器
     */
    @Bean
    @ConditionalOnMissingBean
    public ModelRouter modelRouter(ModelProperties properties) {
        log.info("正在配置模型路由器，策略: {}", properties.getRoutingStrategy());
        return new DefaultModelRouter(properties.getRoutingStrategy());
    }

    /**
     * 配置模型客户端注册器
     */
    @Configuration
    public static class ModelClientRegistrarConfiguration {

        /**
         * 注册所有模型客户端到路由器
         */
        @Bean
        public ModelClientRegistrar modelClientRegistrar(ModelRouter modelRouter, ModelProperties properties,
                                                        java.util.List<ModelClient> clients) {
            log.info("正在注册模型客户端到路由器");
            return new ModelClientRegistrar(modelRouter, properties, clients);
        }
    }

    /**
     * 模型客户端注册器，负责将所有客户端注册到路由器
     */
    public static class ModelClientRegistrar {

        private final ModelRouter modelRouter;
        private final ModelProperties properties;

        public ModelClientRegistrar(ModelRouter modelRouter, ModelProperties properties,
                                   java.util.List<ModelClient> clients) {
            this.modelRouter = modelRouter;
            this.properties = properties;

            // 注册所有客户端
            for (ModelClient client : clients) {
                modelRouter.addClient(client);
                log.info("已注册模型客户端: {}", client.getClass().getSimpleName());
            }

            // 设置默认客户端
            String defaultProvider = properties.getDefaultProvider();
            if (defaultProvider != null && !defaultProvider.isEmpty()) {
                for (String clientKey : modelRouter.getAllClients().keySet()) {
                    if (clientKey.toLowerCase().contains(defaultProvider.toLowerCase())) {
                        modelRouter.setDefaultClient(clientKey);
                        log.info("已设置默认模型客户端: {}", clientKey);
                        break;
                    }
                }
            }
        }
    }
}
