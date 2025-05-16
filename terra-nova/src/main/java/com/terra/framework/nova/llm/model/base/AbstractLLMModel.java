package com.terra.framework.nova.llm.model.base;

import com.terra.framework.common.util.httpclient.HttpClientUtils;

/**
 * @author Zeus
 * @date 2025年05月16日 09:44
 * @description AbstractLLMModel
 */
public abstract class AbstractLLMModel implements LLMModel {
    protected final HttpClientUtils httpClientUtils;

    protected AbstractLLMModel(HttpClientUtils httpClientUtils) {
        this.httpClientUtils = httpClientUtils;
    }
}
