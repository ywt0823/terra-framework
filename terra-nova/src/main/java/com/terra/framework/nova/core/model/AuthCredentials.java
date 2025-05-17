package com.terra.framework.nova.core.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * 认证凭证
 *
 * @author terra-nova
 */
@Data
@Builder
public class AuthCredentials {

    /**
     * 认证类型
     */
    private AuthType authType;

    /**
     * 认证头名称
     */
    private String headerName;

    /**
     * 认证头值
     */
    private String headerValue;

    /**
     * 认证令牌
     */
    private String token;

    /**
     * 过期时间戳
     */
    private long expiresAt;

    /**
     * 额外头信息
     */
    @Builder.Default
    private Map<String, String> extraHeaders = new HashMap<>();

    /**
     * 额外查询参数
     */
    @Builder.Default
    private Map<String, String> extraQueryParams = new HashMap<>();

    /**
     * 检查凭证是否有效
     *
     * @return 是否有效
     */
    public boolean isValid() {
        if (expiresAt <= 0) {
            return true; // 未设置过期时间视为永久有效
        }
        return System.currentTimeMillis() < expiresAt;
    }

    /**
     * 添加额外头信息
     *
     * @param name 名称
     * @param value 值
     * @return 当前凭证实例
     */
    public AuthCredentials addExtraHeader(String name, String value) {
        extraHeaders.put(name, value);
        return this;
    }

    /**
     * 添加额外查询参数
     *
     * @param name 名称
     * @param value 值
     * @return 当前凭证实例
     */
    public AuthCredentials addExtraQueryParam(String name, String value) {
        extraQueryParams.put(name, value);
        return this;
    }

    /**
     * 创建API密钥认证凭证
     *
     * @param apiKey API密钥
     * @return 认证凭证
     */
    public static AuthCredentials ofApiKey(String apiKey) {
        return AuthCredentials.builder()
                .authType(AuthType.API_KEY)
                .headerName("Authorization")
                .headerValue("Bearer " + apiKey)
                .token(apiKey)
                .build();
    }
}
