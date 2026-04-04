package com.terra.framework.crust.handler;

import com.terra.framework.common.result.Result;
import com.terra.framework.common.util.result.ResultUtils;
import com.terra.framework.crust.annotation.IgnoreResponseAdvice;
import org.springframework.core.MethodParameter;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import java.util.Arrays;
import java.util.Objects;

/**
 * @author ywt
 * @description
 * @date 2021年08月13日 13:51
 */
@RestControllerAdvice
public class ResponseAdvice implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter methodParameter, Class<? extends HttpMessageConverter<?>> aClass) {
        return true;
    }

    @Override
    public Object beforeBodyWrite(Object resultValue,
                                  MethodParameter methodParameter,
                                  MediaType mediaType, Class<? extends HttpMessageConverter<?>> aClass,
                                  ServerHttpRequest serverHttpRequest,
                                  ServerHttpResponse serverHttpResponse) {
        if (resultValue instanceof Result) {
            return resultValue;
        }
        if (resultValue instanceof ModelAndView) {
            return resultValue;
        }
        if (resultValue instanceof Model) {
            return resultValue;
        }
        if (Arrays.stream(Objects.requireNonNull(methodParameter.getMethod()).getDeclaredAnnotations()).anyMatch(annotation -> annotation.annotationType().equals(IgnoreResponseAdvice.class))) {
            return resultValue;
        }
        return ResultUtils.success(resultValue);
    }

}
