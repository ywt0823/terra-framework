package com.terra.framework.crust.interceptor;

import com.terra.framework.crust.trace.MDCTraceManager;
import com.terra.framework.crust.trace.TraceContextHolder;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;

import java.io.IOException;
import java.util.Map;

public class TraceIdRequestInterceptor implements ClientHttpRequestInterceptor {

    private final TraceContextHolder contextHolder;
    
    public TraceIdRequestInterceptor(TraceContextHolder contextHolder) {
        this.contextHolder = contextHolder;
    }

    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body,
                                        ClientHttpRequestExecution execution) throws IOException {
        HttpHeaders headers = request.getHeaders();
        
        // 使用MDCTraceManager获取所有跟踪头信息
        Map<String, String> traceHeaders = MDCTraceManager.getTraceHeaders();
        
        // 添加到请求头
        traceHeaders.forEach(headers::add);
        
        return execution.execute(request, body);
    }
}
