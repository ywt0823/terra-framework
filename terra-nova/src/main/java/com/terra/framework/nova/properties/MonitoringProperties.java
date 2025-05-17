package com.terra.framework.nova.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "terra.nova.monitoring")
public class MonitoringProperties {
    private boolean enabled = true;
}