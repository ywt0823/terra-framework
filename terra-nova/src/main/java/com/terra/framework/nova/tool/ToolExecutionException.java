package com.terra.framework.nova.tool;

/**
 * 工具执行异常
 *
 * @author terra-nova
 */
public class ToolExecutionException extends Exception {
    
    /**
     * 工具名称
     */
    private final String toolName;
    
    /**
     * 错误代码
     */
    private final String errorCode;
    
    /**
     * 构造函数
     *
     * @param toolName 工具名称
     * @param message 错误消息
     */
    public ToolExecutionException(String toolName, String message) {
        this(toolName, null, message);
    }
    
    /**
     * 构造函数
     *
     * @param toolName 工具名称
     * @param errorCode 错误代码
     * @param message 错误消息
     */
    public ToolExecutionException(String toolName, String errorCode, String message) {
        super(message);
        this.toolName = toolName;
        this.errorCode = errorCode;
    }
    
    /**
     * 构造函数
     *
     * @param toolName 工具名称
     * @param errorCode 错误代码
     * @param message 错误消息
     * @param cause 原因
     */
    public ToolExecutionException(String toolName, String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.toolName = toolName;
        this.errorCode = errorCode;
    }
    
    /**
     * 获取工具名称
     *
     * @return 工具名称
     */
    public String getToolName() {
        return toolName;
    }
    
    /**
     * 获取错误代码
     *
     * @return 错误代码
     */
    public String getErrorCode() {
        return errorCode;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("ToolExecutionException: ");
        sb.append("[").append(toolName).append("] ");
        
        if (errorCode != null) {
            sb.append("(").append(errorCode).append(") ");
        }
        
        sb.append(getMessage());
        return sb.toString();
    }
} 