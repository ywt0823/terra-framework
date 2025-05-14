package com.terra.framework.crust.filter;

import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

/**
 * @author yangwt
 * @date 2022/12/29 13:22
 **/
@Configuration
@WebFilter(filterName = "cors", urlPatterns = "/*", dispatcherTypes = DispatcherType.REQUEST)
@Slf4j
public class TerraCorsFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse res = (HttpServletResponse) servletResponse;
        if (StringUtils.isEmpty(res.getHeader("Access-Control-Allow-Origin"))) {
            res.addHeader("Access-Control-Allow-Origin", "*");
        }
        if (StringUtils.isEmpty(res.getHeader("Access-Control-Allow-Credentials"))) {
            res.addHeader("Access-Control-Allow-Credentials", "true");
        }
        if (StringUtils.isEmpty(res.getHeader("Access-Control-Allow-Methods"))) {
            res.addHeader("Access-Control-Allow-Methods", "GET, POST, DELETE, PUT");
        }
        if (StringUtils.isEmpty(res.getHeader("Access-Control-Allow-Headers"))) {
            res.addHeader("Access-Control-Allow-Headers", "Content-Type,X-CAF-Authorization-Token,sessionToken,X-TOKEN,Authorization");
        }
        if ("OPTIONS".equals(((HttpServletRequest) servletRequest).getMethod())) {
            servletResponse.getWriter().println("Success");
            return;
        }
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }
}
