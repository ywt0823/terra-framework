package com.terra.framework.nova.llm.properties;

import com.terra.framework.nova.llm.blend.MergeStrategy;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "terra.nova.blend")
public class BlenderProperties {
    private boolean enabled = true;
    private MergeStrategy mergeStrategy = MergeStrategy.WEIGHTED;
    private boolean autoAddModels = true;
    private int threadPoolSize = 0;
}
