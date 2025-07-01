package com.terra.framework.geyser.util;

import java.io.Serializable;

/**
 * 一个用于表示缓存中"空"值的可序列化对象.
 * <p>
 * 当数据库查询结果为null时，此对象被存入缓存，以防止缓存穿透攻击.
 * 它使用单例模式，以减少不必要的对象创建.
 * </p>
 *
 * @author Terra Framework Team
 */
public final class CacheNull implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 单例实例
     */
    public static final CacheNull INSTANCE = new CacheNull();

    private CacheNull() {
        // 私有构造，确保单例
    }

    /**
     * 防止反序列化时创建新对象
     * @return 单例实例
     */
    private Object readResolve() {
        return INSTANCE;
    }
} 