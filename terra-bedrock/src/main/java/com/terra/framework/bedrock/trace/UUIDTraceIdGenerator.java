package com.terra.framework.bedrock.trace;

import java.util.UUID;

/**
 * 默认的TraceIdGenerator实现，使用UUID.
 * <p>
 * 这个类本身不注册为Spring组件，而是由自动配置类 (TerraTraceAutoConfiguration)
 * 通过@Bean的方式来提供，以便于被用户自定义的Bean覆盖.
 *
 * @author Terra Framework Team
 */
public class UUIDTraceIdGenerator implements TraceIdGenerator {

    /**
     * 生成一个32位的、移除了连字符的UUID字符串.
     *
     * @return a {@link java.lang.String} object.
     */
    @Override
    public String generate() {
        return UUID.randomUUID().toString().replace("-", "");
    }
} 