package com.terra.framework.strata.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;

/**
 * @author ywt
 * @description
 * @date 2022年12月25日 11:06
 */
@Slf4j
public class RedisKeyHelper {

    private static final String KEY_PREFIX = "terra";

    private static final String SEPARATOR = ":";

    private final Environment environment;

    private final String applicationName;

    public String getKeyName(String... keyNames) {
        String activeProfile = environment.getActiveProfiles()[0];
        log.info("获取redisKey ,环境: {} ,服务名称: {} ", activeProfile, applicationName);
        String redisKey = KEY_PREFIX + SEPARATOR +
                activeProfile + SEPARATOR +
                applicationName + SEPARATOR +
                String.join(SEPARATOR, keyNames);
        log.info("获取redisKey的值为: {}", redisKey);
        return redisKey;
    }

    public RedisKeyHelper(Environment environment, String applicationName) {
        this.environment = environment;
        this.applicationName = applicationName;
    }
} 