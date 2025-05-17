package com.terra.framework.nova.exception;

import lombok.Getter;

/**
 * 模型异常
 *
 * @author terra-nova
 */
public class ModelException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 错误类型
     */
    @Getter
    private final ErrorType errorType;

    /**
     * 厂商错误代码
     */
    @Getter
    private final String vendorCode;

    /**
     * 创建一个模型异常
     *
     * @param message 异常消息
     */
    public ModelException(String message) {
        this(message, null, ErrorType.UNKNOWN_ERROR, null);
    }

    /**
     * 创建一个模型异常
     *
     * @param message 异常消息
     * @param cause 原因异常
     */
    public ModelException(String message, Throwable cause) {
        this(message, cause, ErrorType.UNKNOWN_ERROR, null);
    }

    /**
     * 创建一个模型异常
     *
     * @param message 异常消息
     * @param errorType 错误类型
     */
    public ModelException(String message, ErrorType errorType) {
        this(message, null, errorType, null);
    }

    /**
     * 创建一个模型异常
     *
     * @param message 异常消息
     * @param cause 原因异常
     * @param errorType 错误类型
     */
    public ModelException(String message, Throwable cause, ErrorType errorType) {
        this(message, cause, errorType, null);
    }

    /**
     * 创建一个模型异常
     *
     * @param message 异常消息
     * @param cause 原因异常
     * @param errorType 错误类型
     * @param vendorCode 厂商错误代码
     */
    public ModelException(String message, Throwable cause, ErrorType errorType, String vendorCode) {
        super(message, cause);
        this.errorType = errorType;
        this.vendorCode = vendorCode;
    }
}
