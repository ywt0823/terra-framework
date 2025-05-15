package com.terra.framework.geyser.options;

import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

/**
 * 缓存操作接口
 * 定义缓存的基本操作，可以适配不同的缓存实现（Guava、Redis等）
 * 
 * @author terra
 */
public interface CacheOperation<K, V> {

    /**
     * 获取缓存，如果不存在则通过callable加载
     *
     * @param key 缓存键
     * @param callable 值加载器
     * @return 缓存值
     */
    V get(K key, Callable<V> callable);
    
    /**
     * 获取缓存，如果不存在则返回null
     *
     * @param key 缓存键
     * @return 缓存值
     */
    V getIfPresent(K key);
    
    /**
     * 设置缓存值
     *
     * @param key 缓存键
     * @param value 缓存值
     */
    void put(K key, V value);
    
    /**
     * 批量设置缓存值
     *
     * @param map 缓存键值对
     */
    void putAll(Map<K, V> map);
    
    /**
     * 删除指定键的缓存
     *
     * @param key 缓存键
     */
    void invalidate(K key);
    
    /**
     * 批量删除缓存
     *
     * @param keys 缓存键列表
     */
    void invalidateAll(List<K> keys);
    
    /**
     * 清空所有缓存
     */
    void invalidateAll();
    
    /**
     * 获取缓存的统计信息
     *
     * @return 统计信息
     */
    String getStats();

    /**
     * 获取缓存名称
     *
     * @return 缓存名称
     */
    String getName();
} 