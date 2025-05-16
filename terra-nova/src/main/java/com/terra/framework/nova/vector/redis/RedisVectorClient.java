package com.terra.framework.nova.vector.redis;

import com.alibaba.fastjson.JSON;
import com.terra.framework.nova.vector.document.Document;
import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;

import java.util.*;

/**
 * Redis向量客户端，提供向量数据的底层存储和检索
 *
 * @author terra-nova
 */
@Slf4j
public class RedisVectorClient {
    
    private final JedisPool jedisPool;
    private final RedisConfig redisConfig;
    
    /**
     * 构造函数
     *
     * @param jedisPool Jedis连接池
     * @param redisConfig Redis配置
     */
    public RedisVectorClient(JedisPool jedisPool, RedisConfig redisConfig) {
        this.jedisPool = jedisPool;
        this.redisConfig = redisConfig;
    }
    
    /**
     * 存储向量
     *
     * @param documentId 文档ID
     * @param vector 向量
     * @param metadata 元数据
     * @return 是否成功
     */
    public boolean storeVector(String documentId, float[] vector, Map<String, Object> metadata) {
        if (documentId == null || vector == null) {
            return false;
        }
        
        String vectorKey = redisConfig.getVectorKey(documentId);
        String metadataKey = redisConfig.getMetadataKey(documentId);
        
        try (Jedis jedis = jedisPool.getResource()) {
            // 存储向量
            Map<String, String> vectorMap = new HashMap<>();
            for (int i = 0; i < vector.length; i++) {
                vectorMap.put(String.valueOf(i), String.valueOf(vector[i]));
            }
            jedis.hset(vectorKey, vectorMap);
            
            // 存储元数据
            if (metadata != null && !metadata.isEmpty()) {
                Map<String, String> metadataMap = new HashMap<>();
                for (Map.Entry<String, Object> entry : metadata.entrySet()) {
                    metadataMap.put(entry.getKey(), JSON.toJSONString(entry.getValue()));
                }
                jedis.hset(metadataKey, metadataMap);
            }
            
            // 将文档ID添加到向量集合
            jedis.sadd(redisConfig.getVectorSetKey(), documentId);
            
            return true;
        } catch (Exception e) {
            log.error("存储向量失败: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * 批量存储向量
     *
     * @param documents 文档列表
     * @param vectors 向量列表
     * @return 成功存储的数量
     */
    public int storeVectors(List<Document> documents, List<float[]> vectors) {
        if (documents == null || vectors == null || documents.size() != vectors.size()) {
            return 0;
        }
        
        int successCount = 0;
        
        for (int i = 0; i < documents.size(); i++) {
            Document document = documents.get(i);
            float[] vector = vectors.get(i);
            
            if (storeVector(document.getId(), vector, document.getMetadata())) {
                successCount++;
            }
        }
        
        return successCount;
    }
    
    /**
     * 检索向量
     *
     * @param documentId 文档ID
     * @return 向量
     */
    public float[] getVector(String documentId) {
        if (documentId == null) {
            return null;
        }
        
        String vectorKey = redisConfig.getVectorKey(documentId);
        
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> vectorMap = jedis.hgetAll(vectorKey);
            
            if (vectorMap == null || vectorMap.isEmpty()) {
                return null;
            }
            
            // 确定向量维度
            int dimension = vectorMap.size();
            float[] vector = new float[dimension];
            
            // 填充向量
            for (int i = 0; i < dimension; i++) {
                String value = vectorMap.get(String.valueOf(i));
                if (value != null) {
                    vector[i] = Float.parseFloat(value);
                }
            }
            
            return vector;
        } catch (Exception e) {
            log.error("检索向量失败: {}", documentId, e);
            return null;
        }
    }
    
    /**
     * 获取元数据
     *
     * @param documentId 文档ID
     * @return 元数据
     */
    public Map<String, Object> getMetadata(String documentId) {
        if (documentId == null) {
            return null;
        }
        
        String metadataKey = redisConfig.getMetadataKey(documentId);
        
        try (Jedis jedis = jedisPool.getResource()) {
            Map<String, String> metadataMap = jedis.hgetAll(metadataKey);
            
            if (metadataMap == null || metadataMap.isEmpty()) {
                return null;
            }
            
            Map<String, Object> metadata = new HashMap<>();
            for (Map.Entry<String, String> entry : metadataMap.entrySet()) {
                metadata.put(entry.getKey(), JSON.parse(entry.getValue()));
            }
            
            return metadata;
        } catch (Exception e) {
            log.error("获取元数据失败: {}", documentId, e);
            return null;
        }
    }
    
    /**
     * 删除向量
     *
     * @param documentId 文档ID
     * @return 是否成功
     */
    public boolean deleteVector(String documentId) {
        if (documentId == null) {
            return false;
        }
        
        String vectorKey = redisConfig.getVectorKey(documentId);
        String metadataKey = redisConfig.getMetadataKey(documentId);
        
        try (Jedis jedis = jedisPool.getResource()) {
            jedis.del(vectorKey);
            jedis.del(metadataKey);
            jedis.srem(redisConfig.getVectorSetKey(), documentId);
            return true;
        } catch (Exception e) {
            log.error("删除向量失败: {}", documentId, e);
            return false;
        }
    }
    
    /**
     * 获取所有文档ID
     *
     * @return 文档ID集合
     */
    public Set<String> getAllDocumentIds() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.smembers(redisConfig.getVectorSetKey());
        } catch (Exception e) {
            log.error("获取所有文档ID失败", e);
            return new HashSet<>();
        }
    }
    
    /**
     * 根据元数据过滤文档ID
     *
     * @param filters 过滤条件
     * @return 符合条件的文档ID集合
     */
    public Set<String> filterByMetadata(Map<String, Object> filters) {
        if (filters == null || filters.isEmpty()) {
            return getAllDocumentIds();
        }
        
        Set<String> allDocIds = getAllDocumentIds();
        Set<String> filteredDocIds = new HashSet<>();
        
        for (String docId : allDocIds) {
            Map<String, Object> metadata = getMetadata(docId);
            if (metadata != null && matchesFilters(metadata, filters)) {
                filteredDocIds.add(docId);
            }
        }
        
        return filteredDocIds;
    }
    
    /**
     * 检查元数据是否匹配过滤条件
     *
     * @param metadata 元数据
     * @param filters 过滤条件
     * @return 是否匹配
     */
    private boolean matchesFilters(Map<String, Object> metadata, Map<String, Object> filters) {
        for (Map.Entry<String, Object> filter : filters.entrySet()) {
            String key = filter.getKey();
            Object expectedValue = filter.getValue();
            
            if (!metadata.containsKey(key)) {
                return false;
            }
            
            Object actualValue = metadata.get(key);
            
            if (!Objects.equals(expectedValue, actualValue)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 清空所有向量数据
     */
    public void clear() {
        try (Jedis jedis = jedisPool.getResource()) {
            Set<String> allDocIds = jedis.smembers(redisConfig.getVectorSetKey());
            
            for (String docId : allDocIds) {
                String vectorKey = redisConfig.getVectorKey(docId);
                String metadataKey = redisConfig.getMetadataKey(docId);
                jedis.del(vectorKey, metadataKey);
            }
            
            jedis.del(redisConfig.getVectorSetKey());
        } catch (Exception e) {
            log.error("清空向量数据失败", e);
        }
    }
    
    /**
     * 获取向量数量
     *
     * @return 向量数量
     */
    public long count() {
        try (Jedis jedis = jedisPool.getResource()) {
            return jedis.scard(redisConfig.getVectorSetKey());
        } catch (Exception e) {
            log.error("获取向量数量失败", e);
            return 0;
        }
    }
} 