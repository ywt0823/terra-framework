package com.terra.framework.autoconfigure.nova.config.prompt;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Terra Prompt Mapper.
 *
 * @author DeavyJones
 */
@ConfigurationProperties(prefix = "terra.nova.prompt")
public class TerraPromptProperties {

    /**
     * Locations to scan for prompt mapper XML files.
     * Defaults to scanning "prompts/" directory in the classpath.
     */
    private String[] mapperLocations = {"prompts/"};

    public String[] getMapperLocations() {
        return mapperLocations;
    }

    public void setMapperLocations(String[] mapperLocations) {
        this.mapperLocations = mapperLocations;
    }
} 