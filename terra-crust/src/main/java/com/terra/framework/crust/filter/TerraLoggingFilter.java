package com.terra.framework.crust.filter;

import com.terra.framework.common.log.LogPattern;
import com.terra.framework.common.util.web.SysEnv;
import com.terra.framework.crust.web.WebUtil;
import com.terra.framework.crust.wrapper.ContentCachingRequestWrapper;
import com.terra.framework.crust.wrapper.ContentCachingResponseWrapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.Setter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.PathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.terra.framework.crust.web.WebUtil.getReqFromParameterMap;


@Setter
public class TerraLoggingFilter extends OncePerRequestFilter {

    private static final Logger logger = LoggerFactory.getLogger(TerraLoggingFilter.class); // 打印控制台日志

    private LogPattern logPattern;

    private String[] excludes;

    private PathMatcher pathMatcher;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 白名单
        if (pathsMatch(request)) {
            filterChain.doFilter(request, response);
            return;
        }

        long start = System.currentTimeMillis();
        // 请求路径
        String path = getPathWithinApplication(request);
        // 请求头信息
        String headers = getHeaders(request);

        byte[] reqBytes = new byte[0];
        byte[] respBytes = new byte[0];

        HttpServletRequest delegateReq = request;
        HttpServletResponse delegateResp = response;

        try {
            if (WebUtil.isRawRequest(request)) {   // 类型是 application/json application/xml
                delegateReq = new ContentCachingRequestWrapper(request);
                delegateResp = new ContentCachingResponseWrapper(response);
                reqBytes = ((ContentCachingRequestWrapper) delegateReq).getContentAsByteArray();
            } else {
                reqBytes = getReqFromParameterMap(request);
            }
            // 入参
            String reqStr = new String(reqBytes, StandardCharsets.UTF_8);
            reqStr = reqStr.replaceAll("[\r\n\t]", ""); // 清除换行和格式
            // 打印入参
            this.beforeRequest(reqStr, path, headers);
            // do filter
            filterChain.doFilter(delegateReq, delegateResp);

            // response
            if (delegateResp instanceof ContentCachingResponseWrapper) {
                respBytes = ((ContentCachingResponseWrapper) delegateResp).getContentAsByteArray();
                response.getOutputStream().write(respBytes);
                response.getOutputStream().flush();
            }
        } finally {
            // 打印出参
            this.afterRequest(start, path, delegateResp, respBytes);
        }
    }


    private void beforeRequest(String reqStr, String path, String headers) {
        if (StringUtils.isEmpty(reqStr)) { // 入参为空
            logger.info(logPattern.formalize("BeforeRequest", "headers", "URI"), headers, path);
        } else { // 入参不为空
            logger.info(logPattern.formalize("BeforeRequest", "headers", "URI", "body"), headers, path, reqStr);
        }
    }

    private void afterRequest(long start, String path, HttpServletResponse delegateResp, byte[] respBytes) throws UnsupportedEncodingException {

        long elapsed = System.currentTimeMillis() - start;

        String hostName = SysEnv.getHostName();//容器pod的名称

        // 打印出参
        if ((WebUtil.isFileStream(delegateResp))) { // 文件流
            logger.info(logPattern.formalize("AfterRequest", "HOSTNAME", "URI", "elapsed"), hostName, path, elapsed + "ms");
            return;
        }

        if (respBytes == null || respBytes.length == 0) { // 没有相应结果
            logger.info(logPattern.formalize("AfterRequest", "HOSTNAME", "Path", "elapsed"), hostName, path, elapsed + "ms");
            return;
        }

        String resStr = new String(respBytes, StandardCharsets.UTF_8);

        logger.info(logPattern.formalize("AfterRequest", "HOSTNAME", "URI", "elapsed", "response"), hostName, path, elapsed + "ms", resStr);
    }

    // 获取请求路径
    protected String getPathWithinApplication(HttpServletRequest request) {
        String contextPath = request.getContextPath();
        String requestUri = request.getRequestURI();
        if (StringUtils.startsWithIgnoreCase(requestUri, contextPath)) {
            String path = requestUri.substring(contextPath.length());
            return (StringUtils.hasText(path) ? path : "/");
        } else {
            return requestUri;
        }
    }

    // Header信息
    protected String getHeaders(HttpServletRequest request) {
        if (logger.isDebugEnabled()) {
            Optional.ofNullable(request.getHeaderNames()).ifPresent(headerNames -> {
                List<String> headers = new ArrayList<>();
                while (headerNames.hasMoreElements()) {
                    String headerName = headerNames.nextElement();
                    headers.add(headerName + "=" + request.getHeader(headerName));
                }
                logger.debug(logPattern.formalize("请求头参数", "headers"), headers.stream().collect(Collectors.joining(";")));
            });
        }
        Map<String, String> headerMap = new HashMap();
        Set<String> awareHeaders = Stream.of("token").collect(Collectors.toSet());
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            final String headName = headerNames.nextElement();
            awareHeaders.stream().filter(h -> h.equalsIgnoreCase(headName)).findFirst().ifPresent(h -> {
                headerMap.put(h, request.getHeader(headName));
            });
        }
        return headerMap.entrySet().stream().map(entry -> entry.getKey() + "=" + entry.getValue()).collect(Collectors.joining(";"));
    }

    // 路径白名单
    private boolean pathsMatch(HttpServletRequest request) {

        if (logger.isDebugEnabled()) {
            logger.debug("白名单：{}", Arrays.toString(excludes));
        }

        if (excludes == null) {
            return false;
        }
        String requestURI = getPathWithinApplication(request);

        for (String path : excludes) {
            if (pathMatcher.match(path, requestURI)) {
                return true;
            }
        }
        return false;
    }
}
