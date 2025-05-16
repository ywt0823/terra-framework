package com.terra.framework.nova.llm.exception;

/**
 * 不支持的模型异常
 */
public class UnsupportedModelException extends RuntimeException {

    public UnsupportedModelException(String message) {
        super(message);
    }

    public UnsupportedModelException(String message, Throwable cause) {
        super(message, cause);
    }
}
