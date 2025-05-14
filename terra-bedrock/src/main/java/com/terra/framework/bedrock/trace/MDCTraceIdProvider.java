package com.terra.framework.bedrock.trace;

import org.slf4j.MDC;

public class MDCTraceIdProvider implements TraceIdProvider<String> {

    @Override
    public String getIfAvailable() {
        return MDC.get(LoggingContext.MDC_TRACE_KEY);
    }
}
