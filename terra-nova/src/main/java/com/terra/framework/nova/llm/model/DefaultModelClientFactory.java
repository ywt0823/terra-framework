package com.terra.framework.nova.llm.model;

import com.terra.framework.common.util.httpclient.HttpClientUtils;
import lombok.extern.slf4j.Slf4j;

/**
 * 默认模型客户端工厂实现
 *
 * @author terra-nova
 */
@Slf4j
public class DefaultModelClientFactory implements ModelClientFactory {

    /**
     * HTTP客户端工具
     */
    private final HttpClientUtils httpClient;

    /**
     * 构造函数
     *
     * @param httpClient HTTP客户端工具
     */
    public DefaultModelClientFactory(HttpClientUtils httpClient) {
        this.httpClient = httpClient;
    }

    @Override
    public AIModel createClient(ModelConfig config) {
        return AIModelFactory.createModel(config, httpClient);
    }
}
