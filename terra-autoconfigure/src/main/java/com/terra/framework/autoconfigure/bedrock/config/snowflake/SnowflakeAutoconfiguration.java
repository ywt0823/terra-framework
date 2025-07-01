package com.terra.framework.autoconfigure.bedrock.config.snowflake;

import com.terra.framework.autoconfigure.bedrock.config.log.LogAutoConfiguration;
import com.terra.framework.autoconfigure.bedrock.properties.snowflake.SnowflakeProperties;
import com.terra.framework.common.log.LogPattern;
import com.terra.framework.common.util.sequence.SnowflakeUtils;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

/**
 * @author yangwt
 * @date 2023/9/2 14:50
 **/
@EnableConfigurationProperties(SnowflakeProperties.class)
@AutoConfiguration(after = LogAutoConfiguration.class)
public class SnowflakeAutoconfiguration {

    @Bean
    public SnowflakeUtils snowflakeUtils(SnowflakeProperties snowflakeProperties,
                                         LogPattern logPattern) {
        SnowflakeUtils.SnowflakeSequence snowflakeSequence = new SnowflakeUtils.SnowflakeSequence(snowflakeProperties.getMaxSequence());
        SnowflakeUtils snowflakeUtils = new SnowflakeUtils(snowflakeSequence);
        logPattern.formalize("自动装配 valhalla-snowflakeUtils 成功");
        return snowflakeUtils;
    }
}
