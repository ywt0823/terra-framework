package com.terra.framework.crust.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yangwt
 * @date 2024/2/4 10:20
 **/
@ConfigurationProperties("valhalla.web.context")
@Data
public class ValhallaWebContextExcludeProperties {

    private String excludes = "/**/*.html,/webjars/**,/swagger-resources,/static/**";

    private Boolean enabled = true;

}
