package com.terra.framework.nova.cache;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Flow.Publisher;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.terra.framework.nova.model.AIModel;
import com.terra.framework.nova.model.Message;
import com.terra.framework.nova.model.ModelInfo;
import com.terra.framework.nova.model.ModelResponse;

/**
 * 模型缓存装饰器，为任何模型添加缓存功能
 *
 * @author terra-nova
 */
public class CachingModelDecorator implements AIModel {

    private static final Logger log = LoggerFactory.getLogger(CachingModelDecorator.class);

    /**
     * 被装饰的模型
     */
    private final AIModel delegate;

    /**
     * 响应缓存
     */
    private final ResponseCache cache;

    /**
     * 是否启用缓存
     */
    private boolean cacheEnabled = true;

    /**
     * 构造函数
     *
     * @param delegate 被装饰的模型
     * @param cache 响应缓存
     */
    public CachingModelDecorator(AIModel delegate, ResponseCache cache) {
        this.delegate = delegate;
        this.cache = cache;
    }

    /**
     * 设置是否启用缓存
     *
     * @param enabled 是否启用
     */
    public void setCacheEnabled(boolean enabled) {
        this.cacheEnabled = enabled;
    }

    @Override
    public ModelResponse generate(String prompt, Map<String, Object> parameters) {
        if (!shouldUseCache(parameters)) {
            return delegate.generate(prompt, parameters);
        }

        String cacheKey = CacheKey.forPrompt(prompt, parameters);
        ModelResponse cachedResponse = cache.get(cacheKey);

        if (cachedResponse != null) {
            log.debug("使用缓存的生成结果: promptLength={}, cacheKey={}", prompt.length(), cacheKey);
            return cachedResponse;
        }

        ModelResponse response = delegate.generate(prompt, parameters);

        if (response != null) {
            cache.put(cacheKey, response);
        }

        return response;
    }

    @Override
    public CompletableFuture<ModelResponse> generateAsync(String prompt, Map<String, Object> parameters) {
        if (!shouldUseCache(parameters)) {
            return delegate.generateAsync(prompt, parameters);
        }

        String cacheKey = CacheKey.forPrompt(prompt, parameters);
        ModelResponse cachedResponse = cache.get(cacheKey);

        if (cachedResponse != null) {
            log.debug("使用缓存的异步生成结果: promptLength={}, cacheKey={}", prompt.length(), cacheKey);
            return CompletableFuture.completedFuture(cachedResponse);
        }

        return delegate.generateAsync(prompt, parameters)
                .thenApply(response -> {
                    if (response != null) {
                        cache.put(cacheKey, response);
                    }
                    return response;
                });
    }

    @Override
    public Publisher<String> generateStream(String prompt, Map<String, Object> parameters) {
        // 流式生成不使用缓存
        return delegate.generateStream(prompt, parameters);
    }

    @Override
    public ModelResponse chat(List<Message> messages, Map<String, Object> parameters) {
        if (!shouldUseCache(parameters)) {
            return delegate.chat(messages, parameters);
        }

        String cacheKey = CacheKey.forMessages(messages, parameters);
        ModelResponse cachedResponse = cache.get(cacheKey);

        if (cachedResponse != null) {
            log.debug("使用缓存的聊天结果: messagesCount={}, cacheKey={}", messages.size(), cacheKey);
            return cachedResponse;
        }

        ModelResponse response = delegate.chat(messages, parameters);

        if (response != null) {
            cache.put(cacheKey, response);
        }

        return response;
    }

    @Override
    public CompletableFuture<ModelResponse> chatAsync(List<Message> messages, Map<String, Object> parameters) {
        if (!shouldUseCache(parameters)) {
            return delegate.chatAsync(messages, parameters);
        }

        String cacheKey = CacheKey.forMessages(messages, parameters);
        ModelResponse cachedResponse = cache.get(cacheKey);

        if (cachedResponse != null) {
            log.debug("使用缓存的异步聊天结果: messagesCount={}, cacheKey={}", messages.size(), cacheKey);
            return CompletableFuture.completedFuture(cachedResponse);
        }

        return delegate.chatAsync(messages, parameters)
                .thenApply(response -> {
                    if (response != null) {
                        cache.put(cacheKey, response);
                    }
                    return response;
                });
    }

    @Override
    public Publisher<String> chatStream(List<Message> messages, Map<String, Object> parameters) {
        // 流式聊天不使用缓存
        return delegate.chatStream(messages, parameters);
    }

    @Override
    public ModelInfo getModelInfo() {
        return delegate.getModelInfo();
    }

    @Override
    public void init() {
        delegate.init();
    }

    @Override
    public void close() {
        delegate.close();
    }

    /**
     * 判断是否应该使用缓存
     *
     * @param parameters 参数
     * @return 是否使用缓存
     */
    private boolean shouldUseCache(Map<String, Object> parameters) {
        if (!cacheEnabled) {
            return false;
        }

        // 如果参数中明确指定了不使用缓存，则跳过缓存
        if (parameters != null && parameters.containsKey("use_cache")) {
            Object useCacheValue = parameters.get("use_cache");
            if (useCacheValue instanceof Boolean) {
                return (Boolean) useCacheValue;
            } else if (useCacheValue instanceof String) {
                return Boolean.parseBoolean((String) useCacheValue);
            }
        }

        return true;
    }
}
