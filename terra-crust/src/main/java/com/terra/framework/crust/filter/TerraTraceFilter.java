package com.terra.framework.crust.filter;

import com.terra.framework.bedrock.trace.TraceIdGenerator;
import com.terra.framework.crust.trace.TraceContextHolder;
import com.terra.framework.crust.web.WebUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;

/**
 * TerraTraceFilter
 *
 * @author zues
 * @version 1.0
 * @since 2020/12/4 18:45
 **/
@Setter
@RequiredArgsConstructor
public class TerraTraceFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TerraTraceFilter.class);

    private final TraceIdGenerator traceIdGenerator;
    private final TraceContextHolder contextHolder;
    private String[] excludes;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        if (isExcludedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            String traceId = extractTraceId(request);
            String parentSpanId = request.getHeader(TraceContextHolder.PARENT_SPAN_ID_KEY);
            String spanId = traceIdGenerator.generate();

            contextHolder.setTrace(traceId, spanId, parentSpanId);

            Map<String, String> traceHeaders = contextHolder.getTraceHeaders();
            traceHeaders.forEach(response::setHeader);
            
            filterChain.doFilter(request, response);
            
        } finally {
            contextHolder.clear();
        }
    }

    private String extractTraceId(HttpServletRequest request) {
        String traceId = request.getHeader(TraceContextHolder.TRACE_ID_KEY);
        if (!StringUtils.hasText(traceId)) {
            traceId = traceIdGenerator.generate();
            logger.debug("Generated new traceId: {}", traceId);
        }
        return traceId;
    }

    private boolean isExcludedPath(HttpServletRequest request) {
        if (excludes == null || excludes.length == 0) {
            return false;
        }

        String path = WebUtil.getPathWithinApplication(request);
        return Arrays.stream(excludes)
                .anyMatch(pattern -> pathMatcher.match(pattern, path));
    }
}
