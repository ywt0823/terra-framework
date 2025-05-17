package com.terra.framework.nova.model;

/**
 * 认证类型枚举
 *
 * @author terra-nova
 */
public enum AuthType {
    /**
     * API密钥认证
     */
    API_KEY,

    /**
     * AK/SK认证（百度等平台使用）
     */
    AK_SK,

    /**
     * OAuth认证
     */
    OAUTH,

    /**
     * Bearer令牌认证
     */
    BEARER_TOKEN,

    /**
     * 基本认证（用户名/密码）
     */
    BASIC_AUTH,

    /**
     * 无认证
     */
    NONE
}
