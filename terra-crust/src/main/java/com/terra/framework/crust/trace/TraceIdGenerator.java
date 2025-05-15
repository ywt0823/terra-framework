package com.terra.framework.crust.trace;

import com.terra.framework.bedrock.trace.UUIDTraceId;
import com.terra.framework.common.util.web.SysEnv;

import java.util.concurrent.ThreadLocalRandom;

public class TraceIdGenerator {

    private static final String HOSTNAME_PREFIX;
    
    static {
        // 获取主机名前缀，用于区分不同服务器生成的ID
        String hostname = SysEnv.getHostName();
        if (hostname != null && hostname.length() > 4) {
            hostname = hostname.substring(0, 4);
        } else if (hostname == null || hostname.isEmpty()) {
            hostname = "terra";
        }
        HOSTNAME_PREFIX = hostname.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
    }

    
    /**
     * 生成全局唯一的跟踪ID
     */
    public String generateTraceId() {
        // 结合UUIDTraceId和主机名前缀，确保分布式环境下的唯一性
        return HOSTNAME_PREFIX + "-" + System.currentTimeMillis() + "-" + UUIDTraceId.create().substring(0, 8);
    }
    
    /**
     * 生成Span ID，标识具体的调用片段
     */
    public String generateSpanId() {
        // 使用6位随机数作为spanId，便于阅读
        return String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
    }
} 