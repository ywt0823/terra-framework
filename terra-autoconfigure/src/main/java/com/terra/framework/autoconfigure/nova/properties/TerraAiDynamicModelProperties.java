package com.terra.framework.autoconfigure.nova.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "spring.ai.dynamic.model")
public class TerraAiDynamicModelProperties {

    private boolean enabled;

}
