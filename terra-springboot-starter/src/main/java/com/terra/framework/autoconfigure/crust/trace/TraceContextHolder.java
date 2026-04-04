package com.terra.framework.autoconfigure.crust.trace;

import lombok.Data;
import org.slf4j.MDC;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.Map;

/**
 * 链路追踪上下文持有器，通过ThreadLocal传递跟踪信息
 */
@Data
public class TraceContextHolder {

    public static final String TRACE_ID_KEY = "X-Trace-Id";
    public static final String SPAN_ID_KEY = "X-Span-Id";
    public static final String PARENT_SPAN_ID_KEY = "X-Parent-Span-Id";

    private final ThreadLocal<String> traceId = new ThreadLocal<>();
    private final ThreadLocal<String> spanId = new ThreadLocal<>();
    private final ThreadLocal<String> parentSpanId = new ThreadLocal<>();

    public void setTrace(String traceId, String spanId, String parentSpanId) {
        setTraceId(traceId);
        setSpanId(spanId);
        setParentSpanId(parentSpanId);
    }

    public void setTraceId(String traceId) {
        if (StringUtils.hasText(traceId)) {
            this.traceId.set(traceId);
            MDC.put(TRACE_ID_KEY, traceId);
        }
    }

    public void setSpanId(String spanId) {
        if (StringUtils.hasText(spanId)) {
            this.spanId.set(spanId);
            MDC.put(SPAN_ID_KEY, spanId);
        }
    }

    public void setParentSpanId(String parentSpanId) {
        if (StringUtils.hasText(parentSpanId)) {
            this.parentSpanId.set(parentSpanId);
            MDC.put(PARENT_SPAN_ID_KEY, parentSpanId);
        }
    }

    public String getTraceId() {
        return traceId.get();
    }

    public String getSpanId() {
        return spanId.get();
    }

    public String getParentSpanId() {
        return parentSpanId.get();
    }

    public Map<String, String> getTraceHeaders() {
        Map<String, String> headers = new HashMap<>();
        if (StringUtils.hasText(getTraceId())) {
            headers.put(TRACE_ID_KEY, getTraceId());
        }
        if (StringUtils.hasText(getSpanId())) {
            headers.put(SPAN_ID_KEY, getSpanId());
        }
        if (StringUtils.hasText(getParentSpanId())) {
            headers.put(PARENT_SPAN_ID_KEY, getParentSpanId());
        }
        return headers;
    }

    public void clear() {
        traceId.remove();
        spanId.remove();
        parentSpanId.remove();

        MDC.remove(TRACE_ID_KEY);
        MDC.remove(SPAN_ID_KEY);
        MDC.remove(PARENT_SPAN_ID_KEY);
    }
}
