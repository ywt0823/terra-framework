package com.terra.framework.geyser.helper;

import com.terra.framework.common.util.common.JsonUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Redis操作类
 *
 * @author vains
 */
@Component
public class RedisHelper {

    @Autowired
    @Qualifier("terra-redisTemplate")
    private StringRedisTemplate redisTemplate;

    /**
     * 设置key的过期时间
     *
     * @param key     缓存key
     * @param timeout 存活时间
     * @param unit    时间单位
     */
    public void setExpire(String key, long timeout, TimeUnit unit) {
        redisTemplate.expire(key, timeout, unit);
    }

    /**
     * 根据key删除缓存
     *
     * @param keys 要删除的key，可变参数列表
     * @return 删除的缓存数量
     */
    public Long delete(String... keys) {
        if (ObjectUtils.isEmpty(keys)) {
            return 0L;
        }
        return redisTemplate.delete(Arrays.asList(keys));
    }

    /**
     * 存入值
     *
     * @param key   缓存中的key
     * @param value 存入的value
     */
    public void set(String key, String value) {
        valueOperations().set(key, value);
    }

    /**
     * 根据key取值
     *
     * @param key 缓存中的key
     * @return 返回键值对应缓存
     */
    public String get(String key) {
        return valueOperations().get(key);
    }

    /**
     * 设置键值并设置过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    过期时间的单位
     */
    public void set(String key, String value, long timeout, TimeUnit unit) {
        valueOperations().set(key, value, timeout, unit);
    }

    /**
     * 设置键值并设置过期时间（单位秒）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间,单位：秒
     */
    public void set(String key, String value, long timeout) {
        this.set(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 根据key获取缓存并删除缓存
     *
     * @param key 要获取缓存的key
     * @return key对应的缓存
     */
    public String getAndDelete(String key) {
        if (ObjectUtils.isEmpty(key)) {
            return null;
        }
        String value = valueOperations().get(key);
        this.delete(key);
        return value;
    }

    /**
     * 往hash类型的数据中存值
     *
     * @param key   缓存中的key
     * @param field hash结构的key
     * @param value 存入的value
     */
    public void setHash(String key, String field, String value) {
        hashOperations().put(key, field, value);
    }

    /**
     * 根据key取值
     *
     * @param key 缓存中的key
     * @return 缓存key对应的hash数据中field属性的值
     */
    public Object getHash(String key, String field) {
        return hashOperations().hasKey(key, field) ? hashOperations().get(key, field) : null;
    }

    /**
     * 以hash格式存入redis
     *
     * @param key   缓存中的key
     * @param value 存入的对象
     */
    public void setHashAll(String key, Object value) {
        Map<String, String> map = JsonUtils.objectCovertToObject(value, Map.class, String.class, Object.class);
        hashOperations().putAll(key, map);
    }

    /**
     * 设置键值并设置过期时间
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间
     * @param unit    过期时间的单位
     */
    public void setHashAll(String key, Object value, long timeout, TimeUnit unit) {
        this.setHashAll(key, value);
        this.setExpire(key, timeout, unit);
    }

    /**
     * 设置键值并设置过期时间（单位秒）
     *
     * @param key     键
     * @param value   值
     * @param timeout 过期时间,单位：秒
     */
    public void setHashAll(String key, Object value, long timeout) {
        this.setHashAll(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 从redis中获取hash类型数据
     *
     * @param key 缓存中的key
     * @return redis 中hash数据
     */
    public Map<String, String> getMapHashAll(String key) {
        return hashOperations().entries(key);
    }

    /**
     * 根据指定clazz类型从redis中获取对应的实例
     *
     * @param key   缓存key
     * @param clazz hash对应java类的class
     * @param <T>   redis中hash对应的java类型
     * @return clazz实例
     */
    public <T> T getHashAll(String key, Class<T> clazz) {
        Map<String, String> entries = hashOperations().entries(key);
        if (ObjectUtils.isEmpty(entries)) {
            return null;
        }
        return JsonUtils.objectCovertToObject(entries, clazz);
    }

    /**
     * 根据key删除缓存
     *
     * @param key    要删除的key
     * @param fields key对应的hash数据的键值(HashKey)，可变参数列表
     * @return hash删除的属性数量
     */
    public Long deleteHashField(String key, String... fields) {
        if (ObjectUtils.isEmpty(key) || ObjectUtils.isEmpty(fields)) {
            return 0L;
        }
        return hashOperations().delete(key, (Object[]) fields);
    }

    /**
     * 将value添加至key对应的列表中
     *
     * @param key   缓存key
     * @param value 值
     */
    public void listPush(String key, String value) {
        listOperations().rightPush(key, value);
    }

    /**
     * 将value添加至key对应的列表中，并添加过期时间
     *
     * @param key     缓存key
     * @param value   值
     * @param timeout key的存活时间
     * @param unit    时间单位
     */
    public void listPush(String key, String value, long timeout, TimeUnit unit) {
        listOperations().rightPush(key, value);
        this.setExpire(key, timeout, unit);
    }

    /**
     * 将value添加至key对应的列表中，并添加过期时间
     * 默认单位是秒(s)
     *
     * @param key     缓存key
     * @param value   值
     * @param timeout key的存活时间
     */
    public void listPush(String key, String value, long timeout) {
        this.listPush(key, value, timeout, TimeUnit.SECONDS);
    }

    /**
     * 将传入的参数列表添加至key的列表中
     *
     * @param key    缓存key
     * @param values 值列表
     * @return 存入数据的长度
     */
    public Long listPushAll(String key, Collection<String> values) {
        return listOperations().rightPushAll(key, values);
    }

    /**
     * 将传入的参数列表添加至key的列表中，并设置key的存活时间
     *
     * @param key     缓存key
     * @param values  值列表
     * @param timeout key的存活时间
     * @param unit    时间单位
     * @return 存入数据的长度
     */
    public Long listPushAll(String key, Collection<String> values, long timeout, TimeUnit unit) {
        Long count = listOperations().rightPushAll(key, values);
        this.setExpire(key, timeout, unit);
        return count;
    }

    /**
     * 将传入的参数列表添加至key的列表中，并设置key的存活时间
     * 默认单位是秒(s)
     *
     * @param key     缓存key
     * @param values  值列表
     * @param timeout key的存活时间
     * @return 存入数据的长度
     */
    public Long listPushAll(String key, Collection<String> values, long timeout) {
        return this.listPushAll(key, values, timeout, TimeUnit.SECONDS);
    }

    /**
     * 根据key获取list列表
     *
     * @param key 缓存key
     * @return key对应的list列表
     */
    public Collection<String> getList(String key) {
        Long size = listOperations().size(key);
        if (size == null || size == 0) {
            return null;
        }
        return listOperations().range(key, 0, (size - 1));
    }

    /**
     * value操作集
     *
     * @return ValueOperations
     */
    private ValueOperations<String, String> valueOperations() {
        return redisTemplate.opsForValue();
    }

    /**
     * hash操作集
     *
     * @return ValueOperations
     */
    private HashOperations<String, String, String> hashOperations() {
        return redisTemplate.opsForHash();
    }

    /**
     * hash操作集
     *
     * @return ValueOperations
     */
    private ListOperations<String, String> listOperations() {
        return redisTemplate.opsForList();
    }

}
