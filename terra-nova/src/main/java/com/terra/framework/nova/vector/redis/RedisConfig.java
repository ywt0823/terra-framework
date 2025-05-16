package com.terra.framework.nova.vector.redis;

import lombok.Data;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Redis配置类
 *
 * @author terra-nova
 */
@Data
public class RedisConfig {

    /**
     * Redis主机
     */
    private String host = "localhost";

    /**
     * Redis端口
     */
    private int port = 6379;

    /**
     * Redis密码
     */
    private String password;

    /**
     * Redis数据库索引
     */
    private int database = 0;

    /**
     * 连接超时时间（毫秒）
     */
    private int timeout = 2000;

    /**
     * 最大连接数
     */
    private int maxTotal = 8;

    /**
     * 最大空闲连接数
     */
    private int maxIdle = 8;

    /**
     * 最小空闲连接数
     */
    private int minIdle = 0;

    /**
     * 向量键前缀
     */
    private String vectorKeyPrefix = "nova:vector:";

    /**
     * 元数据键前缀
     */
    private String metadataKeyPrefix = "nova:metadata:";

    /**
     * 向量集合键
     */
    private String vectorSetKey = "nova:vector:set";

    /**
     * 创建Jedis连接池
     *
     * @return Jedis连接池
     */
    public JedisPool createJedisPool() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(maxTotal);
        poolConfig.setMaxIdle(maxIdle);
        poolConfig.setMinIdle(minIdle);
        poolConfig.setTestOnBorrow(true);

        if (password != null && !password.isEmpty()) {
            return new JedisPool(poolConfig, host, port, timeout, password, database);
        } else {
            return new JedisPool(poolConfig, host, port, timeout, null, database);
        }
    }

    /**
     * 生成向量键
     *
     * @param documentId 文档ID
     * @return 向量键
     */
    public String getVectorKey(String documentId) {
        return vectorKeyPrefix + documentId;
    }

    /**
     * 生成元数据键
     *
     * @param documentId 文档ID
     * @return 元数据键
     */
    public String getMetadataKey(String documentId) {
        return metadataKeyPrefix + documentId;
    }
}
