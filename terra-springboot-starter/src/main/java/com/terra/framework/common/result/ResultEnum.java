package com.terra.framework.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

/**
 * @author ywt
 * @date 2019年2月9日 08:57:14
 */
@Getter
@AllArgsConstructor
public enum ResultEnum implements ErrorType {
    /**
     * 操作成功
     **/
    SUCCESS(HttpStatus.OK.value(), "操作成功"),
    /**
     * 服务异常
     **/
    FAIL(1000, "系统异常，请稍后重试"),

    UNAUTHORIZED(HttpStatus.UNAUTHORIZED.value(), "未授权"),

    FORBIDDEN(HttpStatus.FORBIDDEN.value(), "禁止访问"),

    ;

    /**
     * 自定义状态码
     **/
    private final int code;
    /**
     * 自定义描述
     **/
    private final String message;

}
