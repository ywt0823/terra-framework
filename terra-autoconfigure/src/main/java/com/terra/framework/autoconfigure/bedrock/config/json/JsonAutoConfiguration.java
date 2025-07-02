package com.terra.framework.autoconfigure.bedrock.config.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jdk8.Jdk8Module;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.terra.framework.autoconfigure.bedrock.config.log.LogAutoConfiguration;
import com.terra.framework.autoconfigure.bedrock.properties.json.JsonProperties;
import com.terra.framework.common.util.common.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.text.SimpleDateFormat;

/**
 * JSON工具自动配置类
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@ConditionalOnClass(JsonUtils.class)
@ConditionalOnProperty(prefix = "terra.json", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(JsonProperties.class)
@AutoConfigureAfter(LogAutoConfiguration.class)
public class JsonAutoConfiguration {

    /**
     * 定义全局的ObjectMapper Bean
     *
     * @param jsonProperties a {@link com.terra.framework.autoconfigure.bedrock.properties.json.JsonProperties} object
     * @return a {@link com.fasterxml.jackson.databind.ObjectMapper} object
     */
    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper(JsonProperties jsonProperties) {
        ObjectMapper mapper = new ObjectMapper();

        // 基础序列化配置
        mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        mapper.configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false);

        // 基础反序列化配置
        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

        // 注册模块
        mapper.registerModule(new Jdk8Module());
        mapper.registerModule(new JavaTimeModule());

        // 根据配置文件中的属性进行自定义配置
        if (jsonProperties.getDateFormat() != null) {
            mapper.setDateFormat(new SimpleDateFormat(jsonProperties.getDateFormat()));
        }

        mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,
            !jsonProperties.getIgnoreUnknownProperties());

        if (!jsonProperties.getIncludeNullValues()) {
            mapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }

        return mapper;
    }

    /**
     * 用于初始化JsonUtils的专用Bean.
     * 它依赖于ObjectMapper Bean，确保在执行初始化时，ObjectMapper已经准备就绪.
     *
     * @param objectMapper the object mapper
     * @return a {@link com.terra.framework.bedrock.config.json.JsonAutoConfiguration.JsonUtilsInitializer} object
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
