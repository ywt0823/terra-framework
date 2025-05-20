package com.terra.framework.bedrock.config.json;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.terra.framework.bedrock.config.log.LogAutoConfiguration;
import com.terra.framework.bedrock.properties.json.JsonProperties;
import com.terra.framework.common.log.LogPattern;
import com.terra.framework.common.util.common.JsonUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
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
@AutoConfiguration(after = LogAutoConfiguration.class)
public class JsonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ObjectMapper objectMapper(JsonProperties jsonProperties) {
        ObjectMapper objectMapper = JsonUtils.createDefaultMapper();
        
        // 根据配置属性对ObjectMapper进行自定义配置
        if (jsonProperties.getDateFormat() != null) {
            objectMapper.setDateFormat(new SimpleDateFormat(jsonProperties.getDateFormat()));
        }
        
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, 
                !jsonProperties.getIgnoreUnknownProperties());
        
        if (!jsonProperties.getIncludeNullValues()) {
            objectMapper.setSerializationInclusion(JsonInclude.Include.NON_NULL);
        }
        
        return objectMapper;
    }

    @Bean
    @ConditionalOnMissingBean
    public JsonUtils jsonUtils(ObjectMapper objectMapper, LogPattern logPattern) {
        JsonUtils jsonUtils = new JsonUtils(objectMapper);
        logPattern.formalize("自动装配 terra-json-JsonUtils 成功");
        return jsonUtils;
    }
} 