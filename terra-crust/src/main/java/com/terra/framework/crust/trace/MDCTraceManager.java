package com.terra.framework.crust.trace;

import com.terra.framework.bedrock.trace.LoggingContext;
import com.terra.framework.bedrock.trace.LoggingContextHolder;
import com.terra.framework.bedrock.trace.TraceHelper;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * MDC Trace管理器，统一管理bedrock和crust两个模块的traceId
 */
public class MDCTraceManager {

    public static final String SPAN_ID_KEY = "spanId";
    public static final String PARENT_SPAN_ID_KEY = "parentSpanId";
    public static final String X_TRACE_ID = "X-Trace-Id";
    public static final String X_SPAN_ID = "X-Span-Id";
    public static final String X_PARENT_SPAN_ID = "X-Parent-Span-Id";

    /**
     * 设置完整的链路追踪信息到MDC
     */
    public static void setTraceInfo(String traceId, String spanId, String parentSpanId) {
        // 使用TraceHelper设置traceId，它会同时更新bedrock的LoggingContextHolder和MDC
        TraceHelper.setTraceId(traceId);

        // 额外设置spanId和parentSpanId
        if (StringUtils.hasText(spanId)) {
            MDC.put(SPAN_ID_KEY, spanId);
        }

        if (StringUtils.hasText(parentSpanId)) {
            MDC.put(PARENT_SPAN_ID_KEY, parentSpanId);
        }
    }

    /**
     * 从MDC清除所有链路追踪信息
     */
    public static void clearTraceInfo() {
        // 使用TraceHelper清除traceId，它会同时清除bedrock的LoggingContextHolder和MDC
        TraceHelper.clearTraceId();

        // 额外清除spanId和parentSpanId
        MDC.remove(SPAN_ID_KEY);
        MDC.remove(PARENT_SPAN_ID_KEY);
    }

    /**
     * 获取当前的traceId
     */
    public static String getTraceId() {
        // 优先从LoggingContextHolder获取，保持与bedrock一致
        String traceId = LoggingContextHolder.get().getTraceId();
        if (!StringUtils.hasText(traceId)) {
            // 如果为空则从MDC获取
            traceId = MDC.get(LoggingContext.MDC_TRACE_KEY);
        }
        return traceId;
    }

    /**
     * 获取当前的spanId
     */
    public static String getSpanId() {
        return MDC.get(SPAN_ID_KEY);
    }

    /**
     * 获取当前的parentSpanId
     */
    public static String getParentSpanId() {
        return MDC.get(PARENT_SPAN_ID_KEY);
    }

    /**
     * 获取所有跟踪相关的HTTP头信息
     */
    public static Map<String, String> getTraceHeaders() {
        Map<String, String> headers = new HashMap<>();

        String traceId = getTraceId();
        if (StringUtils.hasText(traceId)) {
            headers.put(LoggingContext.HTTP_TRACE_KEY, traceId);
            headers.put(X_TRACE_ID, traceId);
        }

        String spanId = getSpanId();
        if (StringUtils.hasText(spanId)) {
            headers.put(X_SPAN_ID, spanId);
        }

        String parentSpanId = getParentSpanId();
        if (StringUtils.hasText(parentSpanId)) {
            headers.put(X_PARENT_SPAN_ID, parentSpanId);
        }

        return headers;
    }

    /**
     * 为异步执行保存当前的追踪上下文
     */
    public static Map<String, String> captureTraceContext() {
        Map<String, String> context = new HashMap<>();

        String traceId = getTraceId();
        if (StringUtils.hasText(traceId)) {
            context.put(LoggingContext.MDC_TRACE_KEY, traceId);
        }

        String spanId = getSpanId();
        if (StringUtils.hasText(spanId)) {
            context.put(SPAN_ID_KEY, spanId);
        }

        String parentSpanId = getParentSpanId();
        if (StringUtils.hasText(parentSpanId)) {
            context.put(PARENT_SPAN_ID_KEY, parentSpanId);
        }

        return context;
    }

    /**
     * 恢复之前保存的追踪上下文
     */
    public static void restoreTraceContext(Map<String, String> context) {
        if (context == null || context.isEmpty()) {
            return;
        }

        String traceId = context.get(LoggingContext.MDC_TRACE_KEY);
        String spanId = context.get(SPAN_ID_KEY);
        String parentSpanId = context.get(PARENT_SPAN_ID_KEY);

        setTraceInfo(traceId, spanId, parentSpanId);
    }
}