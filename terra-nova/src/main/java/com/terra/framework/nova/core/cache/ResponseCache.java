package com.terra.framework.nova.core.cache;

import com.terra.framework.nova.core.model.ModelResponse;

/**
 * 响应缓存接口
 *
 * @author terra-nova
 */
public interface ResponseCache {

    /**
     * 获取缓存响应
     *
     * @param cacheKey 缓存键
     * @return 缓存的响应，如果不存在则返回null
     */
    ModelResponse get(String cacheKey);

    /**
     * 将响应存入缓存
     *
     * @param cacheKey 缓存键
     * @param response 响应
     * @param ttlSeconds 过期时间（秒）
     */
    void put(String cacheKey, ModelResponse response, int ttlSeconds);

    /**
     * 将响应存入缓存（使用默认过期时间）
     *
     * @param cacheKey 缓存键
     * @param response 响应
     */
    void put(String cacheKey, ModelResponse response);

    /**
     * 检查缓存键是否存在
     *
     * @param cacheKey 缓存键
     * @return 是否存在
     */
    boolean contains(String cacheKey);

    /**
     * 从缓存中移除指定键
     *
     * @param cacheKey 缓存键
     */
    void remove(String cacheKey);

    /**
     * 清空所有缓存
     */
    void clear();

    /**
     * 获取缓存大小
     *
     * @return 缓存大小
     */
    int size();
}
