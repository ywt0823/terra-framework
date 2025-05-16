package com.terra.framework.nova.model.client;

/**
 * 模型客户端状态枚举
 *
 * @author terra-nova
 */
public enum ClientStatus {
    /**
     * 就绪状态
     */
    READY,
    /**
     * 忙碌状态
     */
    BUSY,
    /**
     * 错误状态
     */
    ERROR,
    /**
     * 离线状态
     */
    OFFLINE,
    /**
     * 初始化中
     */
    INITIALIZING,
    /**
     * 关闭中
     */
    CLOSING
} 