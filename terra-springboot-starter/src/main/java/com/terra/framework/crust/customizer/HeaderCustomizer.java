package com.terra.framework.crust.customizer;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.Ordered;


/**
 * HeaderCustomizer 自定义Header处理
 * RequestHandlerInterceptor 拦截器中执行
 *
 * @author ywt
 * @version 2.0
 * @since 2024.2.4
 **/

@FunctionalInterface
public interface HeaderCustomizer extends Ordered {

    void customize(HttpServletRequest request);

    @Override
    default int getOrder() {
        return 0;
    }
}
