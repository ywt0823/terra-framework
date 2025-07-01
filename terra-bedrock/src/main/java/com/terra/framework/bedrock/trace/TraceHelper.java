package com.terra.framework.bedrock.trace;

import org.slf4j.MDC;

import java.util.Objects;

/**
 * 手动配置 traceId， 应用于异步场景.
 * <p>
 * 这是一个纯粹的工具类，需要通过外部调用 {@link #init(TraceIdGenerator)} 方法来初始化.
 * 在Spring环境中，这个初始化过程应该由一个Spring-aware的模块（如terra-crust）来完成.
 *
 * @author shawn
 * @version V1.0
 * @since 2020/10/28 15:27
 */
public final class TraceHelper {

    private static TraceIdGenerator traceIdGenerator;

    private TraceHelper() {
        // 私有构造函数，防止实例化
    }

    /**
     * 初始化工具类，注入TraceIdGenerator实例.
     * 此方法应该在应用程序启动时被调用一次.
     *
     * @param generator a {@link com.terra.framework.bedrock.trace.TraceIdGenerator} object
     */
    public static void init(TraceIdGenerator generator) {
        if (TraceHelper.traceIdGenerator == null) {
            TraceHelper.traceIdGenerator = generator;
        }
    }

    private static void ensureInitialized() {
        Objects.requireNonNull(traceIdGenerator, "TraceHelper has not been initialized. " +
            "Please ensure it is initialized at application startup.");
    }

    public static String getTraceId() {
        ensureInitialized();
        String traceId = MDC.get(LoggingContext.MDC_TRACE_KEY);
        return traceId == null ? traceIdGenerator.generate() : traceId;
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
        ensureInitialized();
        StringBuilder sb = new StringBuilder();
        String childTraceId = traceIdGenerator.generate();
        setTraceId(traceId == null ? childTraceId : sb.append(traceId).append(":").append(childTraceId).toString());
    }

    // 清除 子traceId
    public static void clearChildTrace(String traceId) {
        ensureInitialized();
        if (traceId == null) {
            setTraceId(traceIdGenerator.generate());
            return;
        }
        setTraceId(traceId.split(":")[0]);
    }
}
