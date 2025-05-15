package com.terra.framework.crust.trace;

import lombok.Getter;

/**
 * 链路追踪上下文持有器，通过ThreadLocal传递跟踪信息
 */
public class TraceContextHolder {
    
    private static final ThreadLocal<TraceContext> CONTEXT_HOLDER = ThreadLocal.withInitial(TraceContext::new);
    
    public String getTraceId() {
        String traceId = CONTEXT_HOLDER.get().getTraceId();
        if (traceId == null) {
            // 如果本地上下文没有，则尝试从MDC获取
            traceId = MDCTraceManager.getTraceId();
            if (traceId != null) {
                setTraceId(traceId);
            }
        }
        return traceId;
    }
    
    public void setTraceId(String traceId) {
        CONTEXT_HOLDER.get().setTraceId(traceId);
    }
    
    public String getSpanId() {
        String spanId = CONTEXT_HOLDER.get().getSpanId();
        if (spanId == null) {
            // 如果本地上下文没有，则尝试从MDC获取
            spanId = MDCTraceManager.getSpanId();
            if (spanId != null) {
                setSpanId(spanId);
            }
        }
        return spanId;
    }
    
    public void setSpanId(String spanId) {
        CONTEXT_HOLDER.get().setSpanId(spanId);
    }
    
    public String getParentSpanId() {
        String parentSpanId = CONTEXT_HOLDER.get().getParentSpanId();
        if (parentSpanId == null) {
            // 如果本地上下文没有，则尝试从MDC获取
            parentSpanId = MDCTraceManager.getParentSpanId();
            if (parentSpanId != null) {
                setParentSpanId(parentSpanId);
            }
        }
        return parentSpanId;
    }
    
    public void setParentSpanId(String parentSpanId) {
        CONTEXT_HOLDER.get().setParentSpanId(parentSpanId);
    }
    
    public TraceContext getContext() {
        return CONTEXT_HOLDER.get();
    }
    
    public void clear() {
        CONTEXT_HOLDER.remove();
    }
    
    @Getter
    public static class TraceContext {
        private String traceId;
        private String spanId;
        private String parentSpanId;
        private long startTime = System.currentTimeMillis();
        
        public void setTraceId(String traceId) {
            this.traceId = traceId;
        }
        
        public void setSpanId(String spanId) {
            this.spanId = spanId;
        }
        
        public void setParentSpanId(String parentSpanId) {
            this.parentSpanId = parentSpanId;
        }
    }
} 