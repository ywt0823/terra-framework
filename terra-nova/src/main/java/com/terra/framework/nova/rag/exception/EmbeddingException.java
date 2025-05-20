package com.terra.framework.nova.rag.exception;

/**
 * 嵌入过程中的异常
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public class EmbeddingException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    /**
     * 创建异常
     *
     * @param message 错误消息
     */
    public EmbeddingException(String message) {
        super(message);
    }

    /**
     * 创建异常
     *
     * @param message 错误消息
     * @param cause 原因
     */
    public EmbeddingException(String message, Throwable cause) {
        super(message, cause);
    }

    /**
     * 创建异常
     *
     * @param cause 原因
     */
    public EmbeddingException(Throwable cause) {
        super(cause);
    }
} 