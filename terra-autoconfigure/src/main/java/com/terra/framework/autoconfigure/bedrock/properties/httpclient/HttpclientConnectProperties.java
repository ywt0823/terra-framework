package com.terra.framework.autoconfigure.bedrock.properties.httpclient;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * HTTP客户端连接配置属性
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Data
@ConfigurationProperties(prefix = "terra.httpclient")
public class HttpclientConnectProperties {

    /**
     * 是否开启HttpClient
     */
    private Boolean enabled = true;

    /**
     * 连接请求超时时间（毫秒）
     */
    private Integer connectionRequestTimeout = 100000;

    /**
     * 连接超时时间（毫秒）
     */
    private Integer connectTimeout = 100000;

    /**
     * Socket超时时间（毫秒）
     */
    private Integer socketTimeout = 100000;

    /**
     * 连接池最大生成连接数
     */
    private Integer requestMaxNum = 500;

    /**
     * 每个路由最大连接数
     */
    private Integer maxPerRoute = 100;

    /**
     * 是否开启自动重试
     */
    private Boolean retryEnabled = true;

    /**
     * 最大重试次数
     */
    private Integer maxRetryCount = 3;

    /**
     * 是否验证SSL证书
     */
    private Boolean validateSslCertificate = true;

    /**
     * 线程池大小
     */
    private Integer threadPoolSize = 100;
}
