package com.terra.framework.crust.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Collections;
import java.util.List;

/**
 * CORS configuration properties.
 *
 * @author AI
 */
@Data
@ConfigurationProperties(prefix = "terra.crust.cors")
public class TerraCorsProperties {

    /**
     * Whether to enable CORS configuration. Defaults to false.
     */
    private boolean enabled = false;

    /**
     * List of allowed origins. E.g. https://example.com.
     * Default is ["*"], which is insecure and should be overridden in production.
     */
    private List<String> allowedOrigins = Collections.singletonList("*");

    /**
     * List of allowed HTTP methods.
     */
    private List<String> allowedMethods = List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH");

    /**
     * List of allowed headers. Default is ["*"].
     */
    private List<String> allowedHeaders = Collections.singletonList("*");

    /**
     * Whether to allow credentials.
     */
    private boolean allowCredentials = true;

    /**
     * The value of the 'max-age' header in the pre-flight response in seconds.
     */
    private long maxAge = 3600L;
    
    /**
     * The path pattern to which this CORS configuration applies.
     */
    private String mapping = "/**";
} 