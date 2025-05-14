package com.terra.framework.bedrock.trace;

import org.slf4j.MDC;

/**
 * 手动配置 traceId， 应用于异步场景
 *
 * @author shawn
 * @version V1.0
 * @since 2020/10/28 15:27
 */
public class TraceHelper {

    public static String getTraceId() {
        String traceId = MDC.get(LoggingContext.MDC_TRACE_KEY);
        return traceId == null ? UUIDTraceId.create() : traceId;
    }

    public static void setTraceId(String traceId) {
        LoggingContextHolder.get().setTraceId(traceId);
        MDC.put(LoggingContext.MDC_TRACE_KEY, traceId);
    }


    public static void clearTraceId() {
        LoggingContextHolder.remove();
        MDC.remove(LoggingContext.MDC_TRACE_KEY);
    }

    // 增加一级 traceId， 用于父子线程
    public static void markChildTraceId(String traceId) {
        StringBuilder sb = new StringBuilder();

        String childTraceId = UUIDTraceId.create();
        setTraceId(traceId == null ? sb.append(childTraceId).toString()
                : sb.append(traceId).append(":").append(childTraceId).toString());
    }

    // 清除 子traceId
    public static void clearChildTrace(String traceId) {
        if (traceId == null) {
            setTraceId(UUIDTraceId.create());
            return;
        }
        setTraceId(traceId.split(":")[0]);
    }
}
