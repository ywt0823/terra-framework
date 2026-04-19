package com.terra.framework.autoconfigure.crust.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code @Scheduled} 任务与 HTTP 一致的 MDC trace/span 注入。
 */
@Data
@ConfigurationProperties(prefix = "terra.trace.scheduled-task-decorator")
public class TerraScheduledTaskTraceProperties {

    /**
     * 是否注册 {@link org.springframework.core.task.TaskDecorator}（Spring Boot 会挂到调度线程池）。
     */
    private boolean enabled = true;
}
