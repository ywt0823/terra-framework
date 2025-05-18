package com.terra.framework.nova.llm.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import lombok.Builder;
import lombok.Data;

/**
 * 重试配置
 *
 * @author terra-nova
 */
@Data
@Builder
public class RetryConfig {

    /**
     * 最大重试次数
     */
    @Builder.Default
    private int maxRetries = 3;

    /**
     * 初始重试延迟（毫秒）
     */
    @Builder.Default
    private long initialDelayMs = 1000;

    /**
     * 最大重试延迟（毫秒）
     */
    @Builder.Default
    private long maxDelayMs = 10000;

    /**
     * 退避乘数
     */
    @Builder.Default
    private double backoffMultiplier = 2.0;

    /**
     * 可重试的错误类型
     */
    @Builder.Default
    private Set<String> retryableErrors = new HashSet<>();

    /**
     * 添加可重试错误类型
     *
     * @param errorTypes 错误类型
     * @return 当前配置实例
     */
    public RetryConfig addRetryableErrors(String... errorTypes) {
        retryableErrors.addAll(Arrays.asList(errorTypes));
        return this;
    }

    /**
     * 创建默认重试配置
     *
     * @return 默认重试配置
     */
    public static RetryConfig defaultConfig() {
        return RetryConfig.builder()
                .maxRetries(3)
                .initialDelayMs(1000)
                .maxDelayMs(10000)
                .backoffMultiplier(2.0)
                .retryableErrors(new HashSet<>(Arrays.asList(
                        "timeout",
                        "rate_limit_exceeded",
                        "server_error",
                        "service_unavailable"
                )))
                .build();
    }
}
