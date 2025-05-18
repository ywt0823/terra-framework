package com.terra.framework.nova.llm.model;

/**
 * 模型状态枚举
 *
 * @author terra-nova
 */
public enum ModelStatus {
    /**
     * 初始化中
     */
    INITIALIZING,

    /**
     * 就绪
     */
    READY,

    /**
     * 忙碌中
     */
    BUSY,

    /**
     * 错误状态
     */
    ERROR,

    /**
     * 关闭中
     */
    CLOSING,

    /**
     * 已离线
     */
    OFFLINE
}
