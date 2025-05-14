package com.terra.framework.common.util.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * 本地缓存工具 <br/>
 *
 * @author Leon Sun
 * @since 2022/4/28
 */
@Slf4j
public class GuavaCacheUtils {

    /**
     * 数据库路由缓存，存储10分钟
     * 2021-10-09 修改为expireAfterAccess
     */
    private static final Cache<Object, Object> dbCfgCache = CacheBuilder.newBuilder().maximumSize(2000).expireAfterAccess(10, TimeUnit.MINUTES).build();

    /**
     * 数据库路由缓存，存储60分钟
     * 当缓存项在指定的时间段内没有被读或写就会被回收
     */
    private static final Cache<Object, Object> accessCache = CacheBuilder.newBuilder().maximumSize(2000).expireAfterAccess(1, TimeUnit.HOURS).build();

    /**
     * 数据库路由缓存，存储1分钟
     */
    private static final Cache<Object, Object> downStreamCache = CacheBuilder.newBuilder().maximumSize(2000).expireAfterWrite(1, TimeUnit.MINUTES).build();


    /**
     * 数据库缓存
     * Callable只有在缓存值不存在时，才会调用
     * 所有类型的Guava Cache，不管有没有自动加载功能，都支持get(K, Callable<V>)方法。
     * 这个方法返回缓存中相应的值，或者用给定的Callable运算并把结果加入到缓存中。
     * 在整个加载方法完成前，缓存项相关的可观察状态都不会更改。
     * 这个方法简便地实现了模式"如果有缓存则返回；否则运算、缓存、然后返回"
     *
     * @param k
     * @param cable
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K, V> V getDbCache(K k, Callable<V> cable) throws ExecutionException {
        return (V) dbCfgCache.get(k, cable);
    }

    /**
     * 数据库缓存
     * Callable只有在缓存值不存在时，才会调用
     * 所有类型的Guava Cache，不管有没有自动加载功能，都支持get(K, Callable<V>)方法。
     * 这个方法返回缓存中相应的值，或者用给定的Callable运算并把结果加入到缓存中。
     * 在整个加载方法完成前，缓存项相关的可观察状态都不会更改。
     * 这个方法简便地实现了模式"如果有缓存则返回；否则运算、缓存、然后返回"
     *
     * @param k
     * @param cable
     * @return
     */
    @SuppressWarnings("unchecked")
    public static <K, V> V getAccessCacheCache(K k, Callable<V> cable) throws ExecutionException {
        return (V) accessCache.get(k, cable);
    }

    /**
     * 移除DbCache
     *
     * @param k
     * @param <K>
     */
    @SuppressWarnings("unchecked")
    public static <K> void invalidateDbCache(K k) {
        try {
            dbCfgCache.invalidate(k);
        } catch (Throwable t) {
            log.error("GuavaCacheUtil | removeDbCache | error", t);
        }
    }

    /**
     * 移除AccessCache
     *
     * @param k
     * @param <K>
     */
    @SuppressWarnings("unchecked")
    public static <K> void invalidateAccessCache(K k) {
        try {
            accessCache.invalidate(k);
        } catch (Throwable t) {
            log.error("GuavaCacheUtil | removeDbCache | error", t);
        }
    }

    /**
     * 移除多个DbCache
     *
     * @param kList
     * @param <K>
     */
    @SuppressWarnings("unchecked")
    public static <K> void invalidateDbCache(List<K> kList) {
        try {
            dbCfgCache.invalidateAll(kList);
        } catch (Throwable t) {
            log.error("GuavaCacheUtil | removeDbCache | error", t);
        }
    }

    /**
     * 失效所有cache
     *
     * @param <K>
     */

    public static <K> void invalidateAllDbCache() {
        try {
            dbCfgCache.invalidateAll();
        } catch (Throwable t) {
            log.error("GuavaCacheUtil | invalidateAll | error", t);
        }
    }

    /**
     * 失效所有cache
     *
     * @param <K>
     */

    public static <K> void invalidateAllAccessCache() {
        try {
            accessCache.invalidateAll();
        } catch (Throwable t) {
            log.error("GuavaCacheUtil | invalidateAll | error", t);
        }
    }


    @SuppressWarnings("unchecked")
    public static <K, V> V getDownStreamCache(K k, Callable<V> cable) throws ExecutionException {
        return (V) downStreamCache.get(k, cable);
    }

    /**
     * 移除
     *
     * @param k
     * @param <K>
     */
    @SuppressWarnings("unchecked")
    public static <K> void invalidateDownStreamCache(K k) {
        try {
            downStreamCache.invalidate(k);
        } catch (Throwable t) {
            log.error("GuavaCacheUtil | removeDownStreamCache | error", t);
        }
    }

    public static <K, V> V getDbCacheIfPresent(K k) {
        try {
            return (V) dbCfgCache.getIfPresent(k);
        } catch (Throwable t) {
            log.error("GuavaCacheUtil | getDbCacheIfPresent | error", t);
        }
        return null;
    }

    public static <K, V> V getAccessCacheIfPresent(K k) {
        try {
            return (V) accessCache.getIfPresent(k);
        } catch (Throwable t) {
            log.error("GuavaCacheUtil | getDbCacheIfPresent | error", t);
        }
        return null;
    }

    /**
     * 往缓存中存plan数据。
     */
    public static void setWriteExpireCache(String cacheKey, Object o) {
        dbCfgCache.put(cacheKey, o);
    }

    /**
     * 往缓存中存场景数据。
     */
    public static void setAccessExpireCache(String cacheKey, Object o) {
        accessCache.put(cacheKey, o);
    }

}
