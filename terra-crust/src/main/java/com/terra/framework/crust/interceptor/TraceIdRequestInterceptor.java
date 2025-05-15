package com.terra.framework.crust.interceptor;

import com.terra.framework.bedrock.trace.LoggingContext;
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
        String traceId = contextHolder.getTraceId();
        String spanId = contextHolder.getSpanId();
        
        HttpHeaders headers = request.getHeaders();
        
        // 添加跟踪信息到请求头
        if (StringUtils.hasText(traceId)) {
            headers.add(LoggingContext.HTTP_TRACE_KEY, traceId);
            headers.add("X-Trace-Id", traceId);
        }
        
        if (StringUtils.hasText(spanId)) {
            // 将当前spanId作为父spanId传递
            headers.add("X-Parent-Span-Id", spanId);
        }
        
        return execution.execute(request, body);
    }
}
