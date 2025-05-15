package com.terra.framework.crust.trace;

import lombok.Getter;

/**
 * 链路追踪上下文持有器，通过ThreadLocal传递跟踪信息
 */
public class TraceContextHolder {
    
    private static final ThreadLocal<TraceContext> CONTEXT_HOLDER = ThreadLocal.withInitial(TraceContext::new);
    
    public String getTraceId() {
        return CONTEXT_HOLDER.get().getTraceId();
    }
    
    public void setTraceId(String traceId) {
        CONTEXT_HOLDER.get().setTraceId(traceId);
    }
    
    public String getSpanId() {
        return CONTEXT_HOLDER.get().getSpanId();
    }
    
    public void setSpanId(String spanId) {
        CONTEXT_HOLDER.get().setSpanId(spanId);
    }
    
    public String getParentSpanId() {
        return CONTEXT_HOLDER.get().getParentSpanId();
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