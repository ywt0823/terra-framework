package com.terra.framework.crust.trace;

import com.terra.framework.bedrock.trace.LoggingContext;
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
        // 使用TraceHelper设置traceId保持与bedrock模块的兼容性
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
        // 使用TraceHelper清除traceId保持与bedrock模块的兼容性
        TraceHelper.clearTraceId();
        
        // 额外清除spanId和parentSpanId
        MDC.remove(SPAN_ID_KEY);
        MDC.remove(PARENT_SPAN_ID_KEY);
    }
    
    /**
     * 获取当前的traceId
     */
    public static String getTraceId() {
        return TraceHelper.getTraceId();
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
     * 获取所有跟踪相关的信息
     */
    public static Map<String, String> getAllTraceInfo() {
        Map<String, String> traceInfo = new HashMap<>();
        traceInfo.put(LoggingContext.MDC_TRACE_KEY, getTraceId());
        
        String spanId = getSpanId();
        if (StringUtils.hasText(spanId)) {
            traceInfo.put(SPAN_ID_KEY, spanId);
        }
        
        String parentSpanId = getParentSpanId();
        if (StringUtils.hasText(parentSpanId)) {
            traceInfo.put(PARENT_SPAN_ID_KEY, parentSpanId);
        }
        
        return traceInfo;
    }
} 