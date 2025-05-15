package com.terra.framework.crust.config;

import com.terra.framework.bedrock.config.log.LogAutoConfiguration;
import com.terra.framework.common.log.LogPattern;
import com.terra.framework.crust.filter.TerraTraceFilter;
import com.terra.framework.crust.properties.TerraTraceProperties;
import com.terra.framework.crust.trace.TraceContextHolder;
import com.terra.framework.crust.trace.TraceDataCollector;
import com.terra.framework.crust.trace.TraceIdGenerator;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;

@Configuration
@EnableConfigurationProperties(TerraTraceProperties.class)
@AutoConfigureAfter(LogAutoConfiguration.class)
public class TerraTraceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TraceIdGenerator traceIdGenerator() {
        return new TraceIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceContextHolder traceContextHolder() {
        return new TraceContextHolder();
    }

    @Bean
    @ConditionalOnProperty(prefix = "terra.trace", name = "collector.enabled", havingValue = "true", matchIfMissing = true)
    public TraceDataCollector traceDataCollector(LogPattern logPattern, TerraTraceProperties traceProperties) {
        return new TraceDataCollector(logPattern, traceProperties);
    }

    @Bean
    public FilterRegistrationBean<TerraTraceFilter> traceFilterRegistration(
            TraceIdGenerator traceIdGenerator,
            TraceContextHolder contextHolder,
            TerraTraceProperties traceProperties) {
        
        FilterRegistrationBean<TerraTraceFilter> registration = new FilterRegistrationBean<>();
        TerraTraceFilter traceFilter = new TerraTraceFilter(traceIdGenerator, contextHolder);
        traceFilter.setExcludes(traceProperties.getExcludes());
        
        registration.setFilter(traceFilter);
        registration.addUrlPatterns("/*");
        registration.setName("terraTraceFilter");
        registration.setOrder(Ordered.HIGHEST_PRECEDENCE + 10);
        
        return registration;
    }
} 