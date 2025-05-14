package com.terra.framework.crust.filter;

import com.terra.framework.bedrock.trace.LoggingContext;
import com.terra.framework.bedrock.trace.TraceHelper;
import com.terra.framework.bedrock.trace.UUIDTraceId;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * AriesTraceFilter
 *
 * @author Shawn
 * @version 1.0
 * @since 2020/12/4 18:45
 **/
public class TerraTraceFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TerraTraceFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        // 设置 TraceId
        String traceId = request.getHeader(LoggingContext.HTTP_TRACE_KEY);
        if (StringUtils.isEmpty(traceId)) {
            traceId = UUIDTraceId.create();
        }
        TraceHelper.setTraceId(traceId);
        logger.debug("设置traceId: {}", traceId);

        try {
            filterChain.doFilter(request, response);
        } finally {
            // 清除traceId
            TraceHelper.clearTraceId();
            logger.debug("清除traceId: {}", traceId);
        }
    }
}
