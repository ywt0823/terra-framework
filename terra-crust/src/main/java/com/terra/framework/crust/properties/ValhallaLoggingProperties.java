package com.terra.framework.crust.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties("valhalla.web.logging")
public class ValhallaLoggingProperties {

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

    private String aspectjExpression;

    private Boolean enabled = true;
}
