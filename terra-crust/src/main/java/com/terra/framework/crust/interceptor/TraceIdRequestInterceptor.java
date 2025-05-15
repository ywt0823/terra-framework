package com.terra.framework.crust.interceptor;

import com.terra.framework.bedrock.trace.LoggingContext;
import com.terra.framework.crust.trace.MDCTraceManager;
import com.terra.framework.crust.trace.TraceContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.util.StringUtils;

import java.io.IOException;

public class TraceIdRequestInterceptor implements ClientHttpRequestInterceptor {

    private final TraceContextHolder contextHolder;
    
    public TraceIdRequestInterceptor(TraceContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                       ClientHttpRequestExecution execution) throws IOException {
        // 获取当前traceId，优先使用TraceContextHolder，保持API兼容
        String traceId = contextHolder.getTraceId();
        if (!StringUtils.hasText(traceId)) {
            traceId = MDCTraceManager.getTraceId();
        }
        
        // 获取当前spanId
        String spanId = contextHolder.getSpanId();
        if (!StringUtils.hasText(spanId)) {
            spanId = MDCTraceManager.getSpanId();
        }
        
        HttpHeaders headers = request.getHeaders();
        
        // 添加跟踪信息到请求头
        if (StringUtils.hasText(traceId)) {
            headers.add(LoggingContext.HTTP_TRACE_KEY, traceId);
            headers.add(MDCTraceManager.X_TRACE_ID, traceId);
        }
        
        if (StringUtils.hasText(spanId)) {
            // 将当前spanId作为父spanId传递
            headers.add(MDCTraceManager.X_PARENT_SPAN_ID, spanId);
        }
        
        return execution.execute(request, body);
    }
}
