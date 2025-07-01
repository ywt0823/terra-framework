package com.terra.framework.autoconfigure.crust.config;

import com.terra.framework.autoconfigure.bedrock.config.log.LogAutoConfiguration;
import com.terra.framework.autoconfigure.crust.filter.TerraLoggingFilter;
import com.terra.framework.autoconfigure.crust.interceptor.RequestHandlerInterceptor;
import com.terra.framework.autoconfigure.crust.interceptor.TraceIdRequestInterceptor;
import com.terra.framework.autoconfigure.crust.interceptor.TraceIdRestTemplateCustomizer;
import com.terra.framework.autoconfigure.crust.properties.TerraCorsProperties;
import com.terra.framework.autoconfigure.crust.properties.TerraLoggingProperties;
import com.terra.framework.autoconfigure.crust.properties.TerraWebContextExcludeProperties;
import com.terra.framework.autoconfigure.crust.trace.TraceContextHolder;
import com.terra.framework.common.log.LogPattern;
import com.terra.framework.crust.customizer.HeaderCustomizer;
import jakarta.servlet.Servlet;
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
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.servlet.DispatcherServlet;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication.Type.SERVLET;

@ConditionalOnWebApplication(type = SERVLET)
@ConditionalOnClass({Servlet.class, DispatcherServlet.class, WebMvcConfigurer.class})
@AutoConfigureAfter({LogAutoConfiguration.class, JacksonAutoConfiguration.class, TerraTraceAutoConfiguration.class})
@EnableConfigurationProperties({TerraLoggingProperties.class, TerraWebContextExcludeProperties.class, TerraCorsProperties.class})
public class TerraWebAutoConfiguration {

    @ConditionalOnClass({HandlerInterceptor.class, WebMvcConfigurer.class})
    @Configuration(proxyBeanMethods = false)
    static class RequestHandlerInterceptorConfiguration {
        @Bean
        @ConditionalOnMissingBean
        public RequestHandlerInterceptor requestHandlerInterceptor(ObjectProvider<HeaderCustomizer> headerCustomizers) {
            RequestHandlerInterceptor requestHandlerInterceptor = new RequestHandlerInterceptor();
            requestHandlerInterceptor.setHeaderCustomizers(headerCustomizers.orderedStream().collect(Collectors.toList()));
            return requestHandlerInterceptor;
        }

        @Bean
        public WebMvcConfigurer terraWebMvcConfigurer(RequestHandlerInterceptor requestHandlerInterceptor, TerraWebContextExcludeProperties terraWebContextExcludeProperties, TerraCorsProperties terraCorsProperties) {
            return new WebMvcConfigurer() {
                @Override
                public void addInterceptors(InterceptorRegistry registry) {
                    if (terraWebContextExcludeProperties.getEnabled()) {
                        registry.addInterceptor(requestHandlerInterceptor)
                            .addPathPatterns("/**")
                            .excludePathPatterns(Arrays.asList(StringUtils.commaDelimitedListToStringArray(terraWebContextExcludeProperties.getExcludes())));
                    } else {
                        registry.addInterceptor(requestHandlerInterceptor)
                            .addPathPatterns("/**");
                    }
                }

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
            };
        }
    }

    @Bean
    public TraceIdRequestInterceptor traceIdRequestInterceptor(TraceContextHolder traceContextHolder) {
        return new TraceIdRequestInterceptor(traceContextHolder);
    }

    @Bean
    public TraceIdRestTemplateCustomizer traceIdRestTemplateCustomizer(TraceIdRequestInterceptor traceIdRequestInterceptor) {
        return new TraceIdRestTemplateCustomizer(traceIdRequestInterceptor);
    }

    /**
     * Rest请求 日志拦截器
     */
    @Bean
    @ConditionalOnProperty(value = "terra.web.logging.enabled", havingValue = "true", matchIfMissing = true)
    @Order(Ordered.HIGHEST_PRECEDENCE + 1)
    @ConditionalOnClass(OncePerRequestFilter.class)
    public TerraLoggingFilter terraLoggingFilter(TerraLoggingProperties terraLoggingProperties, LogPattern logPattern) {
        return new TerraLoggingFilter(terraLoggingProperties, logPattern);
    }
}
