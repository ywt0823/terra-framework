package com.terra.framework.common.exception;

import lombok.Getter;

/**
 * HTTP客户端异常
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Getter
public class HttpClientException extends TerraBaseException {
    
    /**
     * HTTP状态码
     */
    private final int statusCode;
    
    /**
     * 请求URL
     */
    private final String requestUrl;
    
    /**
     * 错误类型
     */
    private final HttpErrorType errorType;
    
    /**
     * HTTP错误类型枚举
     */
    public enum HttpErrorType {
        /**
         * 连接超时
         */
        CONNECTION_TIMEOUT(5001, "连接超时"),
        
        /**
         * 读取超时
         */
        READ_TIMEOUT(5002, "读取超时"),
        
        /**
         * 服务器错误
         */
        SERVER_ERROR(5003, "服务器错误"),
        
        /**
         * 客户端错误
         */
        CLIENT_ERROR(5004, "客户端错误"),
        
        /**
         * 重定向错误
         */
        REDIRECT_ERROR(5005, "重定向错误"),
        
        /**
         * SSL/TLS错误
         */
        SSL_ERROR(5006, "SSL/TLS错误"),
        
        /**
         * 未知错误
         */
        UNKNOWN_ERROR(5999, "未知错误");
        
        private final int code;
        private final String message;
        
        HttpErrorType(int code, String message) {
            this.code = code;
            this.message = message;
        }
        
        public int getCode() {
            return code;
        }
        
        public String getMessage() {
            return message;
        }
    }
    
    /**
     * 构造方法
     *
     * @param errorType 错误类型
     * @param message 错误消息
     * @param requestUrl 请求URL
     * @param statusCode HTTP状态码
     */
    public HttpClientException(HttpErrorType errorType, String message, String requestUrl, int statusCode) {
        super(errorType.getCode(), message);
        this.errorType = errorType;
        this.requestUrl = requestUrl;
        this.statusCode = statusCode;
    }
    
    /**
     * 构造方法
     *
     * @param errorType 错误类型
     * @param message 错误消息
     * @param requestUrl 请求URL
     * @param statusCode HTTP状态码
     * @param cause 原因
     */
    public HttpClientException(HttpErrorType errorType, String message, String requestUrl, int statusCode, Throwable cause) {
        super(errorType.getCode(), message);
        this.errorType = errorType;
        this.requestUrl = requestUrl;
        this.statusCode = statusCode;
        initCause(cause);
    }
    
    /**
     * 创建连接超时异常
     *
     * @param requestUrl 请求URL
     * @param cause 原因
     * @return HttpClientException
     */
    public static HttpClientException connectionTimeout(String requestUrl, Throwable cause) {
        return new HttpClientException(HttpErrorType.CONNECTION_TIMEOUT, 
                "连接超时: " + requestUrl, requestUrl, 0, cause);
    }
    
    /**
     * 创建读取超时异常
     *
     * @param requestUrl 请求URL
     * @param cause 原因
     * @return HttpClientException
     */
    public static HttpClientException readTimeout(String requestUrl, Throwable cause) {
        return new HttpClientException(HttpErrorType.READ_TIMEOUT, 
                "读取超时: " + requestUrl, requestUrl, 0, cause);
    }
    
    /**
     * 创建服务器错误异常
     *
     * @param requestUrl 请求URL
     * @param statusCode HTTP状态码
     * @param response 响应内容
     * @return HttpClientException
     */
    public static HttpClientException serverError(String requestUrl, int statusCode, String response) {
        return new HttpClientException(HttpErrorType.SERVER_ERROR, 
                "服务器错误 [" + statusCode + "]: " + response, requestUrl, statusCode);
    }
    
    /**
     * 创建客户端错误异常
     *
     * @param requestUrl 请求URL
     * @param statusCode HTTP状态码
     * @param response 响应内容
     * @return HttpClientException
     */
    public static HttpClientException clientError(String requestUrl, int statusCode, String response) {
        return new HttpClientException(HttpErrorType.CLIENT_ERROR, 
                "客户端错误 [" + statusCode + "]: " + response, requestUrl, statusCode);
    }
} 