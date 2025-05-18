package com.terra.framework.nova.llm.model;

import java.util.HashMap;
import java.util.Map;
import lombok.Builder;
import lombok.Data;

/**
 * 认证配置
 *
 * @author terra-nova
 */
@Data
@Builder
public class AuthConfig {

    /**
     * 认证类型
     */
    private AuthType authType;

    /**
     * API密钥
     */
    private String apiKey;

    /**
     * API密钥ID（百度等平台使用）
     */
    private String apiKeyId;

    /**
     * API密钥密钥（百度等平台使用）
     */
    private String apiKeySecret;

    /**
     * 认证令牌
     */
    private String authToken;

    /**
     * 组织ID（OpenAI使用）
     */
    private String organizationId;

    /**
     * 项目ID（部分平台使用）
     */
    private String projectId;

    /**
     * 额外认证参数
     */
    @Builder.Default
    private Map<String, String> extraParams = new HashMap<>();

    /**
     * 添加额外参数
     *
     * @param key 参数名
     * @param value 参数值
     * @return 当前配置实例
     */
    public AuthConfig addExtraParam(String key, String value) {
        this.extraParams.put(key, value);
        return this;
    }

    /**
     * 创建API密钥认证配置
     *
     * @param apiKey API密钥
     * @return 认证配置
     */
    public static AuthConfig ofApiKey(String apiKey) {
        return AuthConfig.builder()
                .authType(AuthType.API_KEY)
                .apiKey(apiKey)
                .build();
    }

    /**
     * 创建百度AI平台认证配置
     *
     * @param apiKeyId 密钥ID（AK）
     * @param apiKeySecret 密钥（SK）
     * @return 认证配置
     */
    public static AuthConfig ofBaidu(String apiKeyId, String apiKeySecret) {
        return AuthConfig.builder()
                .authType(AuthType.AK_SK)
                .apiKeyId(apiKeyId)
                .apiKeySecret(apiKeySecret)
                .build();
    }
}
