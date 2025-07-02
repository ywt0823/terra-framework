package com.terra.framework.autoconfigure.bedrock.config.log;

import com.terra.framework.common.log.LogPattern;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;

public class LogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LogPattern.class)
    public LogPattern logPattern() {
        return new LogPattern();
    }


}
