package com.terra.framework.nova.core.model;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpRequest;
import org.apache.hc.core5.http.message.BasicHeader;

/**
 * 默认认证提供者实现
 *
 * @author terra-nova
 */
@Slf4j
public class DefaultAuthProvider implements AuthProvider {

    /**
     * 认证配置
     */
    private final AuthConfig authConfig;

    /**
     * 认证凭证
     */
    private volatile AuthCredentials credentials;

    /**
     * 构造函数
     *
     * @param authConfig 认证配置
     */
    public DefaultAuthProvider(AuthConfig authConfig) {
        this.authConfig = authConfig;
        initCredentials();
    }

    /**
     * 初始化认证凭证
     */
    private void initCredentials() {
        if (authConfig == null) {
            credentials = null;
            return;
        }

        switch (authConfig.getAuthType()) {
            case API_KEY:
                credentials = AuthCredentials.ofApiKey(authConfig.getApiKey());
                break;
            case AK_SK:
                credentials = AuthCredentials.builder()
                        .authType(AuthType.AK_SK)
                        .token(authConfig.getApiKeyId() + ":" + authConfig.getApiKeySecret())
                        .build();
                break;
            case BEARER_TOKEN:
                credentials = AuthCredentials.builder()
                        .authType(AuthType.BEARER_TOKEN)
                        .headerName("Authorization")
                        .headerValue("Bearer " + authConfig.getAuthToken())
                        .token(authConfig.getAuthToken())
                        .build();
                break;
            case BASIC_AUTH:
                credentials = AuthCredentials.builder()
                        .authType(AuthType.BASIC_AUTH)
                        .headerName("Authorization")
                        .headerValue("Basic " + authConfig.getAuthToken())
                        .token(authConfig.getAuthToken())
                        .build();
                break;
            case NONE:
                credentials = null;
                break;
            default:
                log.warn("不支持的认证类型: {}", authConfig.getAuthType());
                credentials = null;
                break;
        }

        // 添加额外参数
        if (credentials != null && authConfig.getExtraParams() != null) {
            for (Map.Entry<String, String> entry : authConfig.getExtraParams().entrySet()) {
                credentials.addExtraHeader(entry.getKey(), entry.getValue());
            }
        }
    }

    @Override
    public AuthCredentials getCredentials() {
        if (credentials == null) {
            initCredentials();
        }
        return credentials;
    }

    @Override
    public void refreshCredentials() {
        // 默认实现不需要刷新
        log.debug("刷新认证凭证");
    }

    @Override
    public <T> T applyCredentials(T request) {
        if (credentials == null) {
            return request;
        }

        if (request instanceof HttpRequest) {
            HttpRequest httpRequest = (HttpRequest) request;

            // 添加认证头
            if (credentials.getHeaderName() != null && credentials.getHeaderValue() != null) {
                httpRequest.setHeader(credentials.getHeaderName(), credentials.getHeaderValue());
            }

            // 添加额外头信息
            if (credentials.getExtraHeaders() != null) {
                for (Map.Entry<String, String> entry : credentials.getExtraHeaders().entrySet()) {
                    httpRequest.setHeader(entry.getKey(), entry.getValue());
                }
            }
        }

        return request;
    }

    /**
     * 创建认证头数组
     *
     * @return 认证头数组
     */
    public Header[] createAuthHeaders() {
        if (credentials == null) {
            return new Header[0];
        }

        // 计算头信息总数
        int headerCount = (credentials.getHeaderName() != null && credentials.getHeaderValue() != null) ? 1 : 0;
        headerCount += (credentials.getExtraHeaders() != null) ? credentials.getExtraHeaders().size() : 0;

        if (headerCount == 0) {
            return new Header[0];
        }

        Header[] headers = new Header[headerCount];
        int index = 0;

        // 添加主认证头
        if (credentials.getHeaderName() != null && credentials.getHeaderValue() != null) {
            headers[index++] = new BasicHeader(credentials.getHeaderName(), credentials.getHeaderValue());
        }

        // 添加额外头信息
        if (credentials.getExtraHeaders() != null) {
            for (Map.Entry<String, String> entry : credentials.getExtraHeaders().entrySet()) {
                headers[index++] = new BasicHeader(entry.getKey(), entry.getValue());
            }
        }

        return headers;
    }
}
