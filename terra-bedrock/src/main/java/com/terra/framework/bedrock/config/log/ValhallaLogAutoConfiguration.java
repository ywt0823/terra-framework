package com.terra.framework.bedrock.config.log;

import com.terra.framework.bedrock.trace.MDCTraceIdProvider;
import com.terra.framework.bedrock.trace.UUIDTraceId;
import com.terra.framework.bedrock.trace.async.TraceRunnable;
import com.terra.framework.common.log.LogPattern;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskDecorator;

@AutoConfiguration
public class ValhallaLogAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LogPattern.class)
    public LogPattern logPattern() {
        return new LogPattern();
    }

    @Bean
    public MDCTraceIdProvider mdcTraceIdProvider() {
        return new MDCTraceIdProvider();
    }


    @Bean
    public TaskDecorator tracingTaskDecorator(MDCTraceIdProvider traceIdProvider) {
        return (task) -> new TraceRunnable(task, traceIdProvider.getIfAvailable(UUIDTraceId::create));
    }

}