package com.terra.framework.nova.rag.exception;

/**
 * 文档加载异常
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public class DocumentLoadException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * 创建异常
     *
     * @param message 错误消息
     */
    public DocumentLoadException(String message) {
        super(message);
    }

    /**
     * 创建异常
     *
     * @param message 错误消息
     * @param cause 原因
     */
    public DocumentLoadException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建异常
     *
     * @param cause 原因
     */
    public DocumentLoadException(Throwable cause) {
        super(cause);
    }
} 