package com.terra.framework.crust.filter;

import com.terra.framework.bedrock.trace.LoggingContext;
import com.terra.framework.crust.trace.MDCTraceManager;
import com.terra.framework.crust.trace.TraceContextHolder;
import com.terra.framework.crust.trace.TraceIdGenerator;
import com.terra.framework.crust.web.WebUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

/**
 * AriesTraceFilter
 *
 * @author zues
 * @version 1.0
 * @since 2020/12/4 18:45
 **/
public class TerraTraceFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TerraTraceFilter.class);

    private final TraceIdGenerator traceIdGenerator;
    private final TraceContextHolder contextHolder;
    private String[] excludes;
    private final PathMatcher pathMatcher = new AntPathMatcher();

    public TerraTraceFilter(TraceIdGenerator traceIdGenerator, TraceContextHolder contextHolder) {
        this.traceIdGenerator = traceIdGenerator;
        this.contextHolder = contextHolder;
    }

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 检查是否需要跳过链路追踪
        if (isExcludedPath(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long startTime = System.currentTimeMillis();
        String traceId = extractTraceId(request);
        String parentSpanId = request.getHeader(MDCTraceManager.X_PARENT_SPAN_ID);
        String spanId = traceIdGenerator.generateSpanId();

        try {
            // 设置上下文信息
            contextHolder.setTraceId(traceId);
            contextHolder.setParentSpanId(parentSpanId);
            contextHolder.setSpanId(spanId);

            // 通过统一的MDCTraceManager设置MDC
            MDCTraceManager.setTraceInfo(traceId, spanId, parentSpanId);

            // 设置响应头，用于调试和跟踪
            response.setHeader(MDCTraceManager.X_TRACE_ID, traceId);
            response.setHeader(MDCTraceManager.X_SPAN_ID, spanId);

            logger.debug("链路追踪: traceId={}, spanId={}, parentSpanId={}, uri={}",
                    traceId, spanId, parentSpanId, request.getRequestURI());

            // 执行过滤链
            filterChain.doFilter(request, response);

        } finally {
            // 记录请求完成时间
            long duration = System.currentTimeMillis() - startTime;
            logger.debug("链路追踪完成: traceId={}, uri={}, duration={}ms", 
                    traceId, request.getRequestURI(), duration);
            
            // 通过统一的MDCTraceManager清除MDC
            MDCTraceManager.clearTraceInfo();
            
            // 清理上下文
            contextHolder.clear();
        }
    }

    private String extractTraceId(HttpServletRequest request) {
        // 优先从请求头获取
        String traceId = request.getHeader(LoggingContext.HTTP_TRACE_KEY);
        if (!StringUtils.hasText(traceId)) {
            traceId = request.getHeader(MDCTraceManager.X_TRACE_ID);
        }

        // 如果没有，则生成新的TraceId
        if (!StringUtils.hasText(traceId)) {
            traceId = traceIdGenerator.generateTraceId();
            logger.debug("生成新的traceId: {}", traceId);
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
