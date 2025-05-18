package com.terra.framework.nova.llm.exception;

/**
 * 错误类型枚举
 *
 * @author terra-nova
 */
public enum ErrorType {
    /**
     * 认证错误
     */
    AUTHENTICATION_ERROR,

    /**
     * 速率限制错误
     */
    RATE_LIMIT_ERROR,

    /**
     * 上下文长度错误
     */
    CONTEXT_LENGTH_ERROR,

    /**
     * 无效请求错误
     */
    INVALID_REQUEST_ERROR,

    /**
     * 模型不可用错误
     */
    MODEL_UNAVAILABLE_ERROR,

    /**
     * 服务器错误
     */
    SERVER_ERROR,

    /**
     * 网络错误
     */
    NETWORK_ERROR,

    /**
     * 超时错误
     */
    TIMEOUT_ERROR,

    /**
     * 内容过滤错误
     */
    CONTENT_FILTER_ERROR,

    /**
     * 终端错误
     */
    INTERRUPTED_ERROR,

    /**
     * 服务异常
     */
    SERVICE_UNAVAILABLE_ERROR,
    /**
     * 未知错误
     */
    UNKNOWN_ERROR
}
