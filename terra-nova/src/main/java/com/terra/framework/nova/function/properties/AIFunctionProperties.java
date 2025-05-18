package com.terra.framework.nova.function.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for AI functions.
 *
 * @author terra
 */
@Data
@ConfigurationProperties(prefix = "terra.nova.function")
public class AIFunctionProperties {

    /**
     * Whether to enable AI function support.
     */
    private boolean enabled = true;

    /**
     * Whether to require parameter annotations.
     */
    private boolean requireParameterAnnotations = true;

    /**
     * Whether to validate parameter types.
     */
    private boolean validateParameterTypes = true;

    /**
     * Whether to cache function metadata.
     */
    private boolean cacheMetadata = true;

    /**
     * Base packages to scan for AI functions.
     */
    private String[] basePackages = {};
} 