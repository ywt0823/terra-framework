package com.terra.framework.nova.prompt.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 缓存提示词服务配置属性
 *
 * @author terra-nova
 */
@ConfigurationProperties(prefix = "terra.nova.prompt.cache")
public class CachingPromptProperties {

    /**
     * 是否启用提示词渲染缓存
     */
    private boolean enabled = true;
    
    /**
     * 缓存过期时间（秒）
     */
    private long ttlSeconds = 3600;
    
    /**
     * 缓存最大容量
     */
    private int maxSize = 1000;

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public long getTtlSeconds() {
        return ttlSeconds;
    }

    public void setTtlSeconds(long ttlSeconds) {
        this.ttlSeconds = ttlSeconds;
    }

    public int getMaxSize() {
        return maxSize;
    }

    public void setMaxSize(int maxSize) {
        this.maxSize = maxSize;
    }
} 