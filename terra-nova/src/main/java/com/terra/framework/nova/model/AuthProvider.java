package com.terra.framework.nova.model;

/**
 * 认证提供者接口
 *
 * @author terra-nova
 */
public interface AuthProvider {

    /**
     * 获取认证信息
     *
     * @return 认证信息
     */
    AuthCredentials getCredentials();

    /**
     * 刷新认证信息（如需要）
     */
    void refreshCredentials();

    /**
     * 应用认证信息到请求
     *
     * @param <T> 请求类型
     * @param request 请求
     * @return 应用认证后的请求
     */
    <T> T applyCredentials(T request);
}
