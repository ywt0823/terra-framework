package com.terra.framework.crust.interceptor;


import com.terra.framework.bedrock.trace.LoggingContext;
import com.terra.framework.bedrock.trace.TraceHelper;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;

public class TraceIdRequestInterceptor implements ClientHttpRequestInterceptor {

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        String traceId = TraceHelper.getTraceId();
        HttpHeaders headers = request.getHeaders();
        headers.add(LoggingContext.MDC_TRACE_KEY, traceId);
        return execution.execute(request, body);
    }
}
