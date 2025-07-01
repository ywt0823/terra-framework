package com.terra.framework.autoconfigure.crust.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("terra.web.logging")
public class TerraLoggingProperties {

    private String[] excludeUrls = {
            "/**/*.ico",
            "/**/*.html",
            "/**/*.css",
            "/**/*.js",
            "/**/*.jpg",
            "/**/*.png",
            "/**/*.bmp",
            "/**/*.gif",
            "/webjars/**",
            "/swagger-resources"
    };

    private int maxPayloadLength = 4096;

    private String aspectjExpression;

    private Boolean enabled = true;
}
