package com.terra.framework.nova.core.properties;

import com.terra.framework.nova.core.model.RetryConfig;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "terra.nova.retry")
public class RetryProperties {
    private boolean enabled = true;
    private int maxRetries = 3;
    private long initialDelayMs = 1000;
    private long maxDelayMs = 10000;
    private double backoffMultiplier = 2.0;

    public RetryConfig toRetryConfig() {
        return RetryConfig.builder()
            .maxRetries(maxRetries)
            .initialDelayMs(initialDelayMs)
            .maxDelayMs(maxDelayMs)
            .backoffMultiplier(backoffMultiplier)
            .build();
    }
}
