package com.terra.framework.autoconfigure.nova.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Terra Prompt Mapper.
 *
 * @author DeavyJones
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "terra.nova.prompt")
public class TerraPromptProperties {

    /**
     * Locations to scan for prompt mapper XML files.
     * Defaults to scanning "prompts/" directory in the classpath.
     */
    private String[] mapperLocations = {"classpath*:/prompts/**/*.xml"};


}
