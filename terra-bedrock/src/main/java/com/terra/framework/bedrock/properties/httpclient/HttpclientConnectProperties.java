package com.terra.framework.bedrock.properties.httpclient;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author ywt
 * @description
 * @date 2022年12月24日 23:45
 */
@Data
@ConfigurationProperties(prefix = "terra.httpclient")
public class HttpclientConnectProperties {

    /**
     * 是否开启valhalla的Httpclient
     */
    private Boolean enabled = false;
    /**
     * httpclient链接配置-链接请求超时时间
     */
    private Integer connectionRequestTimeout = 1000;
    /**
     * httpclient链接配置-链接超时时间
     */
    private Integer connectTimeout = 1000;
    /**
     * httpclient链接配置-链接socket超时时间
     */
    private Integer socketTimeout = 1000;

    /**
     * 连接池最大生成连接数
     */
    private Integer requestMaxNum = 100;
    /**
     * 默认设置route最大连接数
     */
    private Integer maxPerRoute = 100;

}
