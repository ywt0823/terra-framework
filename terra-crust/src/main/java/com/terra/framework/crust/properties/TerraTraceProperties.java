package com.terra.framework.crust.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Data
@ConfigurationProperties(prefix = "terra.trace")
public class TerraTraceProperties {

    /**
     * 是否启用链路追踪
     */
    private boolean enabled = true;

    /**
     * 不需要进行链路追踪的URL路径
     */
    private String[] excludes = {
            "/**/*.ico",
            "/**/*.html",
            "/**/*.css",
            "/**/*.js",
            "/**/*.jpg",
            "/**/*.png",
            "/webjars/**",
            "/swagger-resources/**"
    };

    /**
     * 链路数据收集器配置
     */
    private CollectorConfig collector = new CollectorConfig();

    @Data
    public static class CollectorConfig {
        /**
         * 是否启用链路数据收集
         */
        private boolean enabled = true;
        
        /**
         * 采样率，范围0-1，表示收集的请求比例
         */
        private double sampleRate = 1.0;
        
        /**
         * 记录的最大链路数量（防止内存溢出）
         */
        private int maxTraceCapacity = 10000;
    }
} 