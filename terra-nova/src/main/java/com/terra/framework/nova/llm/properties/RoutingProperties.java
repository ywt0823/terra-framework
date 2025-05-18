package com.terra.framework.nova.llm.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "terra.nova.routing")
public class RoutingProperties {
    private boolean enabled = true;
    private int healthCheckFailureThreshold = 3;
    private int healthCheckRecoveryThreshold = 2;
}
