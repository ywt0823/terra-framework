package com.terra.framework.autoconfigure.crust.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Spring MVC 异步请求（含 SseEmitter）的 TaskExecutor 与超时。
 */
@Data
@ConfigurationProperties(prefix = "terra.web.mvc-async")
public class TerraWebMvcAsyncProperties {

    /**
     * 是否用虚拟线程执行器替换默认 SimpleAsyncTaskExecutor，并设置异步超时。
     */
    private boolean enabled = true;

    /**
     * 与常见 SseEmitter 超时对齐，避免先于客户端被框架切断。
     */
    private long timeoutMs = 300_000L;

    private String threadNamePrefix = "mvc-async-";
}
