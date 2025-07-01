package com.terra.framework.crust.config;

import com.terra.framework.bedrock.config.log.LogAutoConfiguration;
import com.terra.framework.common.log.LogPattern;
import com.terra.framework.crust.customizer.HeaderCustomizer;
import com.terra.framework.crust.filter.TerraLoggingFilter;
import com.terra.framework.crust.interceptor.RequestHandlerInterceptor;
import com.terra.framework.crust.interceptor.TraceIdRequestInterceptor;
import com.terra.framework.crust.interceptor.TraceIdRestTemplateCustomizer;
import com.terra.framework.crust.properties.TerraCorsProperties;
import com.terra.framework.crust.properties.ValhallaLoggingProperties;
import com.terra.framework.crust.properties.ValhallaWebContextExcludeProperties;
import com.terra.framework.crust.trace.TraceContextHolder;
import jakarta.servlet.Servlet;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.boot.autoconfigure.jackson.JacksonAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.util.StringUtils;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@Configuration
@RequiredArgsConstructor
@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@AutoConfigureAfter({LogAutoConfiguration.class, JacksonAutoConfiguration.class, TerraTraceAutoConfiguration.class})
@EnableConfigurationProperties({ValhallaLoggingProperties.class, ValhallaWebContextExcludeProperties.class, TerraCorsProperties.class})
public class TerraWebAutoConfiguration implements WebMvcConfigurer {

    private final ValhallaWebContextExcludeProperties valhallaWebContextExcludeProperties;
    private final TerraCorsProperties terraCorsProperties;
    private final RequestHandlerInterceptor requestHandlerInterceptor;

    @Bean
    @ConditionalOnMissingBean
    public RequestHandlerInterceptor requestHandlerInterceptor(ObjectProvider<HeaderCustomizer> headerCustomizers) {
        RequestHandlerInterceptor requestHandlerInterceptor = new RequestHandlerInterceptor();
        requestHandlerInterceptor.setHeaderCustomizers(headerCustomizers.orderedStream().collect(Collectors.toList()));
        return requestHandlerInterceptor;
    }

    @Bean
    public TraceIdRequestInterceptor traceIdRequestInterceptor(TraceContextHolder traceContextHolder) {
        return new TraceIdRequestInterceptor(traceContextHolder);
    }
    
    @Bean
    public TraceIdRestTemplateCustomizer traceIdRestTemplateCustomizer(TraceIdRequestInterceptor traceIdRequestInterceptor) {
        return new TraceIdRestTemplateCustomizer(traceIdRequestInterceptor);
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (valhallaWebContextExcludeProperties.getEnabled()) {
            registry.addInterceptor(requestHandlerInterceptor)
                    .addPathPatterns("/**")
                    .excludePathPatterns(Arrays.asList(StringUtils.commaDelimitedListToStringArray(valhallaWebContextExcludeProperties.getExcludes())));
        } else {
            registry.addInterceptor(requestHandlerInterceptor)
                    .addPathPatterns("/**");
        }
    }

    /**
     * Rest请求 日志拦截器
     */
    @Bean
    @ConditionalOnProperty(value = "valhalla.web.logging.enabled", havingValue = "true", matchIfMissing = true)
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    public TerraLoggingFilter terraLoggingFilter(ValhallaLoggingProperties valhallaLoggingProperties, LogPattern logPattern) {
        return new TerraLoggingFilter(valhallaLoggingProperties, logPattern);
    }

    /**
     * Configures CORS (Cross-Origin Resource Sharing) mappings.
     * This method reads settings from {@link TerraCorsProperties} to apply a global CORS policy.
     *
     * @param registry the CorsRegistry to which the configuration is to be added.
     */
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        if (!terraCorsProperties.isEnabled()) {
            return;
        }
        registry.addMapping(terraCorsProperties.getMapping())
                .allowedOrigins(terraCorsProperties.getAllowedOrigins().toArray(new String[0]))
                .allowedMethods(terraCorsProperties.getAllowedMethods().toArray(new String[0]))
                .allowedHeaders(terraCorsProperties.getAllowedHeaders().toArray(new String[0]))
                .allowCredentials(terraCorsProperties.isAllowCredentials())
                .maxAge(terraCorsProperties.getMaxAge());
    }

}