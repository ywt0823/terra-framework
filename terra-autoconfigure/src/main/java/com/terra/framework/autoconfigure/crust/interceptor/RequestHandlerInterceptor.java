package com.terra.framework.autoconfigure.crust.interceptor;

import com.terra.framework.crust.customizer.HeaderCustomizer;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.util.LambdaSafe;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;

import static com.terra.framework.crust.web.WebUtil.getPathWithinApplication;


/**
 * @author ywt
 * @description
 * @date 2022年12月25日 20:53
 */
@Slf4j
public class RequestHandlerInterceptor implements HandlerInterceptor {


    private String[] excludes;

    private List<HeaderCustomizer> headerCustomizers;

    protected PathMatcher pathMatcher = new AntPathMatcher();

    public void setExcludes(String[] excludes) {
        this.excludes = excludes;
    }

    public void setHeaderCustomizers(List<HeaderCustomizer> headerCustomizers) {
        this.headerCustomizers = headerCustomizers;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response,
                             Object handler) {
        // 白名单
        if (pathsMatch(request)) {
            return true;
        }
        LambdaSafe.callbacks(HeaderCustomizer.class, headerCustomizers, request).invoke((customizer) -> customizer.customize(request));
        return true;
    }


    private boolean pathsMatch(HttpServletRequest request) {
        log.debug("GatewayRequestInterceptor白名单：{}", Arrays.toString(excludes));
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
