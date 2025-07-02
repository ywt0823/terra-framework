package com.terra.framework.autoconfigure.crust.config;

import com.terra.framework.autoconfigure.bedrock.config.log.LogAutoConfiguration;
import com.terra.framework.autoconfigure.crust.filter.TerraTraceFilter;
import com.terra.framework.autoconfigure.crust.properties.TerraTraceProperties;
import com.terra.framework.autoconfigure.crust.trace.TraceContextHolder;
import com.terra.framework.autoconfigure.crust.trace.TraceDataCollector;
import com.terra.framework.bedrock.trace.TraceHelper;
import com.terra.framework.bedrock.trace.TraceIdGenerator;
import com.terra.framework.bedrock.trace.UUIDTraceIdGenerator;
import com.terra.framework.common.log.LogPattern;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.core.Ordered;

@EnableConfigurationProperties(TerraTraceProperties.class)
@AutoConfigureAfter(LogAutoConfiguration.class)
public class TerraTraceAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public TraceIdGenerator traceIdGenerator() {
        return new UUIDTraceIdGenerator();
    }

    @Bean
    @ConditionalOnMissingBean
    public TraceContextHolder traceContextHolder() {
        return new TraceContextHolder();
    }

    /**
     * 用于初始化TraceHelper的专用Bean.
     * 它依赖于TraceIdGenerator Bean，确保在执行初始化时，TraceIdGenerator已经准备就绪.
     */
    @Bean
    @ConditionalOnMissingBean
    public TraceHelperInitializer traceHelperInitializer(TraceIdGenerator traceIdGenerator) {
        return new TraceHelperInitializer(traceIdGenerator);
    }

    @Bean
    @ConditionalOnMissingBean
    @ConditionalOnProperty(prefix = "terra.trace", name = "collector.enabled", havingValue = "true", matchIfMissing = true)
    public TraceDataCollector traceDataCollector(LogPattern logPattern, TerraTraceProperties traceProperties) {
        return new TraceDataCollector(logPattern, traceProperties);
    }

    @Bean
    @ConditionalOnMissingBean
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

    /**
     * 初始化TraceHelper的内部类
     */
    private static class TraceHelperInitializer {
        public TraceHelperInitializer(TraceIdGenerator traceIdGenerator) {
            TraceHelper.init(traceIdGenerator);
        }
    }
}
