package com.terra.framework.autoconfigure.bedrock.config.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.terra.framework.autoconfigure.bedrock.config.log.LogAutoConfiguration;
import com.terra.framework.autoconfigure.bedrock.properties.json.JsonProperties;
import com.terra.framework.common.util.common.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;

/**
 * JSON工具自动配置类
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@AutoConfiguration
@ConditionalOnClass(JsonUtils.class)
@ConditionalOnProperty(prefix = "terra.json", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JsonProperties.class)
@AutoConfigureAfter({LogAutoConfiguration.class, JacksonAutoConfiguration.class})
public class JsonAutoConfiguration {

    /**
     * 通过 Jackson2ObjectMapperBuilderCustomizer 应用 terra.json.*，与 Spring Boot 默认 {@link ObjectMapper} 协同。
     */
    @Bean
    public Jackson2ObjectMapperBuilderCustomizer terraJacksonCustomizer(JsonProperties jsonProperties) {
        return builder -> {
            builder.serializationInclusion(
                Boolean.TRUE.equals(jsonProperties.getIncludeNullValues())
                    ? JsonInclude.Include.ALWAYS
                    : JsonInclude.Include.NON_NULL);
            builder.featuresToDisable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
            builder.featuresToDisable(SerializationFeature.FAIL_ON_EMPTY_BEANS);
            if (Boolean.TRUE.equals(jsonProperties.getIgnoreUnknownProperties())) {
                builder.featuresToDisable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            } else {
                builder.featuresToEnable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
            }
            if (jsonProperties.getDateFormat() != null && !jsonProperties.getDateFormat().isEmpty()) {
                builder.simpleDateFormat(jsonProperties.getDateFormat());
            }
        };
    }

    /**
     * 用于初始化JsonUtils的专用Bean.
     * 它依赖于ObjectMapper Bean，确保在执行初始化时，ObjectMapper已经准备就绪.
     *
     * @param objectMapper the object mapper
     * @return initializer
     */
    @Bean
    public JsonUtilsInitializer jsonUtilsInitializer(ObjectMapper objectMapper) {
        return new JsonUtilsInitializer(objectMapper);
    }

    /**
     * The type Json utils initializer.
     */
    @Slf4j
    private static class JsonUtilsInitializer {
        /**
         * Instantiates a new Json utils initializer.
         *
         * @param objectMapper the object mapper
         */
        public JsonUtilsInitializer(ObjectMapper objectMapper) {
            JsonUtils.init(objectMapper);
            log.info("JsonUtils has been successfully initialized by Spring's ObjectMapper.");
        }
    }
}
