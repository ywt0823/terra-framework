package com.terra.framework.crust.interceptor;

import org.springframework.boot.web.client.RestTemplateCustomizer;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.List;

public class TraceIdRestTemplateCustomizer implements RestTemplateCustomizer {

    private final ClientHttpRequestInterceptor interceptor;

    public TraceIdRestTemplateCustomizer(ClientHttpRequestInterceptor requestInterceptor) {
        interceptor = requestInterceptor;
    }

    @Override
    public void customize(RestTemplate restTemplate) {

        List<ClientHttpRequestInterceptor> existingInterceptors = restTemplate.getInterceptors();
        if (!existingInterceptors.contains(this.interceptor)) {
            List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
            interceptors.add(this.interceptor);
            interceptors.addAll(existingInterceptors);
            restTemplate.setInterceptors(interceptors);
        }
    }
}
