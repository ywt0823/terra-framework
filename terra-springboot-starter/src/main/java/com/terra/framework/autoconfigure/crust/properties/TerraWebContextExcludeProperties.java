package com.terra.framework.autoconfigure.crust.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author yangwt
 * @date 2024/2/4 10:20
 **/
@ConfigurationProperties("terra.web.context")
@Data
public class TerraWebContextExcludeProperties {

    private String excludes = "/**/*.html,/webjars/**,/swagger-resources,/static/**";

    private Boolean enabled = true;

}
