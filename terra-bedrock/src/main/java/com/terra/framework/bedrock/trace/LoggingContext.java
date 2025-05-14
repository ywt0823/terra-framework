package com.terra.framework.bedrock.trace;

public class LoggingContext {

    // ziroom request tracing key
    public static final String HTTP_TRACE_KEY = "X-VT-ID";

    // slf4j MDC key
    public static final String MDC_TRACE_KEY = "__traceId";

    private String traceId;

    public String getTraceId() {
        return traceId;
    }

    public void setTraceId(String traceId) {
        this.traceId = traceId;
    }
}
