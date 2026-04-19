package com.terra.framework.autoconfigure.crust.config;

import com.terra.framework.autoconfigure.crust.properties.TerraScheduledTaskTraceProperties;
import com.terra.framework.autoconfigure.crust.trace.TraceContextHolder;
import com.terra.framework.bedrock.trace.TraceIdGenerator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.TaskDecorator;

/**
 * 为 {@link org.springframework.scheduling.annotation.Scheduled} 注入 trace/span 到
 * {@link TraceContextHolder}（与 HTTP 请求的 MDC 字段一致）。
 */
@AutoConfiguration
@AutoConfigureAfter(TerraTraceAutoConfiguration.class)
@EnableConfigurationProperties(TerraScheduledTaskTraceProperties.class)
@ConditionalOnClass(TaskDecorator.class)
@ConditionalOnProperty(prefix = "terra.trace.scheduled-task-decorator", name = "enabled", havingValue = "true", matchIfMissing = true)
@ConditionalOnBean({TraceIdGenerator.class, TraceContextHolder.class})
public class TerraSchedulingTraceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(TaskDecorator.class)
    public TaskDecorator scheduledTraceTaskDecorator(
            TraceIdGenerator traceIdGenerator,
            TraceContextHolder traceContextHolder) {
        return runnable -> () -> {
            String traceId = traceIdGenerator.generate();
            String spanId = traceIdGenerator.generate();
            try {
                traceContextHolder.setTrace(traceId, spanId, null);
                runnable.run();
            } finally {
                traceContextHolder.clear();
            }
        };
    }
}
