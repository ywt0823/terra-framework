package com.terra.framework.crust.filter;

import com.terra.framework.common.log.LogPattern;
import com.terra.framework.crust.properties.ValhallaLoggingProperties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor
public class TerraLoggingFilter extends OncePerRequestFilter {

    private final ValhallaLoggingProperties loggingProperties;
    private final LogPattern logPattern;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // Check if the request path should be excluded from logging
        if (isExcluded(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Use Spring's ContentCaching wrappers
        ContentCachingRequestWrapper requestWrapper = new ContentCachingRequestWrapper(request);
        ContentCachingResponseWrapper responseWrapper = new ContentCachingResponseWrapper(response);

        long startTime = System.currentTimeMillis();
        String requestURI = request.getRequestURI();

        try {
            filterChain.doFilter(requestWrapper, responseWrapper);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            String requestBody = getRequestBody(requestWrapper);
            String responseBody = getResponseBody(responseWrapper);

            logRequest(requestURI, request.getMethod(), getHeaders(request), requestBody);
            logResponse(requestURI, responseWrapper.getStatus(), duration, responseBody);
            
            // IMPORTANT: Copy the cached response body to the actual response
            responseWrapper.copyBodyToResponse();
        }
    }

    private void logRequest(String uri, String method, String headers, String body) {
        String message = String.format("Request: %s %s, Headers: [%s], Body: %s", method, uri, headers, body);
        log.info(logPattern.formalize("WebRequest", "message"), message);
    }

    private void logResponse(String uri, int status, long duration, String body) {
        String message = String.format("Response: %s, Status: %d, Duration: %dms, Body: %s", uri, status, duration, body);
        log.info(logPattern.formalize("WebResponse", "message"), message);
    }

    private String getRequestBody(ContentCachingRequestWrapper request) {
        byte[] buf = request.getContentAsByteArray();
        if (buf.length > 0) {
            return contentToString(buf, loggingProperties.getMaxPayloadLength(), request.getCharacterEncoding());
        }
        return "[EMPTY]";
    }

    private String getResponseBody(ContentCachingResponseWrapper response) {
        byte[] buf = response.getContentAsByteArray();
        if (buf.length > 0) {
            return contentToString(buf, loggingProperties.getMaxPayloadLength(), response.getCharacterEncoding());
        }
        return "[EMPTY]";
    }

    private String contentToString(byte[] buf, int maxLength, String characterEncoding) {
        try {
            String content = new String(buf, characterEncoding);
            if (content.length() > maxLength) {
                return content.substring(0, maxLength) + "...(truncated)";
            }
            return content;
        } catch (UnsupportedEncodingException e) {
            return "[Unsupported Encoding]";
        }
    }

    private String getHeaders(HttpServletRequest request) {
        Map<String, String> headerMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String headerName = headerNames.nextElement();
            headerMap.put(headerName, request.getHeader(headerName));
        }
        return headerMap.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + entry.getValue())
                .collect(Collectors.joining("; "));
    }

    private boolean isExcluded(HttpServletRequest request) {
        return Arrays.stream(loggingProperties.getExcludeUrls())
                .anyMatch(pattern -> pathMatcher.match(pattern, request.getRequestURI()));
    }
}
