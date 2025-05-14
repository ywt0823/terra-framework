package com.terra.framework.geyser.helper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/**
 * @author ywt
 * @description
 * @date 2022年12月25日 11:06
 */
@Component
@Slf4j
public class RedisKeyHelper {

    private static final String KEY_PREFIX = "valhalla";

    private static final String SEPARATOR = ":";

    @Autowired
    private Environment environment;

    @Value("${spring.application.name}")
    private String applicationName;

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
}
