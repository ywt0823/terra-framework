package com.terra.framework.common.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

import static org.apache.hc.core5.http.HttpStatus.*;


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
    SUCCESS(SC_OK, "操作成功"),
    /**
     * 服务异常
     **/
    FAIL(1000, "系统异常，请稍后重试"),

    UNAUTHORIZED(SC_UNAUTHORIZED, "未授权"),

    FORBIDDEN(SC_FORBIDDEN, "禁止访问"),

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
