package com.terra.framework.nova.core.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "terra.nova.cache")
public class CacheProperties {
    private boolean enabled = true;
    private int defaultTtlSeconds = 3600;
}
