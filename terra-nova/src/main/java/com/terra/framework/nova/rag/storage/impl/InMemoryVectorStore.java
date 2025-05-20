package com.terra.framework.nova.rag.storage.impl;

import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.storage.SearchResult;
import com.terra.framework.nova.rag.storage.VectorStore;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.Collectors;

/**
 * 内存向量存储实现
 * 适用于小规模数据集或开发测试环境
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Slf4j
public class InMemoryVectorStore implements VectorStore {
    
    private final Map<String, Document> documents = new ConcurrentHashMap<>();
    private final Map<String, float[]> embeddings = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    @Override
    public void addDocuments(List<Document> documentList, List<float[]> embeddingsList) {
        if (documentList == null || embeddingsList == null || documentList.size() != embeddingsList.size()) {
            throw new IllegalArgumentException("文档列表和嵌入向量列表必须非空且大小相同");
        }
        
        lock.writeLock().lock();
        try {
            for (int i = 0; i < documentList.size(); i++) {
                Document document = documentList.get(i);
                float[] embedding = embeddingsList.get(i);
                
                if (document == null || document.getId() == null) {
                    log.warn("跳过空文档或ID为空的文档");
                    continue;
                }
                
                documents.put(document.getId(), document);
                embeddings.put(document.getId(), embedding);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public List<SearchResult> similaritySearch(float[] queryEmbedding, int topK) {
        return similaritySearch(queryEmbedding, topK, null);
    }
    
    @Override
    public List<SearchResult> similaritySearch(float[] queryEmbedding, int topK, Map<String, Object> filter) {
        if (queryEmbedding == null || topK <= 0) {
            return Collections.emptyList();
        }
        
        lock.readLock().lock();
        try {
            // 计算所有文档向量与查询向量的相似度
            List<ScoredId> scoredIds = new ArrayList<>();
            
            embeddings.forEach((id, embedding) -> {
                Document doc = documents.get(id);
                
                // 应用过滤条件
                if (filter != null && !filter.isEmpty() && doc != null) {
                    Map<String, Object> metadata = doc.getMetadata();
                    if (metadata == null || !matchesFilter(metadata, filter)) {
                        return;
                    }
                }
                
                float similarity = cosineSimilarity(queryEmbedding, embedding);
                scoredIds.add(new ScoredId(id, similarity));
            });
            
            // 按相似度降序排序并截取前topK个
            return scoredIds.stream()
                    .sorted(Comparator.comparing(ScoredId::getScore).reversed())
                    .limit(topK)
                    .map(scoredId -> {
                        Document doc = documents.get(scoredId.getId());
                        return SearchResult.of(doc, scoredId.getScore());
                    })
                    .collect(Collectors.toList());
        } finally {
            lock.readLock().unlock();
        }
    }
    
    @Override
    public void deleteDocuments(List<String> ids) {
        if (ids == null || ids.isEmpty()) {
            return;
        }
        
        lock.writeLock().lock();
        try {
            for (String id : ids) {
                documents.remove(id);
                embeddings.remove(id);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            documents.clear();
            embeddings.clear();
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public int size() {
        lock.readLock().lock();
        try {
            return documents.size();
        } finally {
            lock.readLock().unlock();
        }
    }
    
    /**
     * 计算两个向量的余弦相似度
     *
     * @param vec1 向量1
     * @param vec2 向量2
     * @return 余弦相似度，范围[0,1]
     */
    private float cosineSimilarity(float[] vec1, float[] vec2) {
        if (vec1.length != vec2.length) {
            throw new IllegalArgumentException("向量维度不匹配: " + vec1.length + " vs " + vec2.length);
        }
        
        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;
        
        for (int i = 0; i < vec1.length; i++) {
            dotProduct += vec1[i] * vec2[i];
            norm1 += vec1[i] * vec1[i];
            norm2 += vec2[i] * vec2[i];
        }
        
        if (norm1 <= 0.0f || norm2 <= 0.0f) {
            return 0.0f;
        }
        
        return dotProduct / (float) (Math.sqrt(norm1) * Math.sqrt(norm2));
    }
    
    /**
     * 检查元数据是否匹配过滤条件
     *
     * @param metadata 元数据
     * @param filter 过滤条件
     * @return 是否匹配
     */
    private boolean matchesFilter(Map<String, Object> metadata, Map<String, Object> filter) {
        for (Map.Entry<String, Object> entry : filter.entrySet()) {
            String key = entry.getKey();
            Object value = entry.getValue();
            
            if (!metadata.containsKey(key)) {
                return false;
            }
            
            Object metadataValue = metadata.get(key);
            
            // 特殊处理集合类型
            if (value instanceof Collection && metadataValue instanceof Collection) {
                @SuppressWarnings("unchecked")
                Collection<Object> filterValues = (Collection<Object>) value;
                @SuppressWarnings("unchecked")
                Collection<Object> metadataValues = (Collection<Object>) metadataValue;
                
                if (!metadataValues.containsAll(filterValues)) {
                    return false;
                }
            } else if (!Objects.equals(value, metadataValue)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 用于存储文档ID和相似度得分
     */
    private static class ScoredId {
        private final String id;
        private final float score;
        
        public ScoredId(String id, float score) {
            this.id = id;
            this.score = score;
        }
        
        public String getId() {
            return id;
        }
        
        public float getScore() {
            return score;
        }
    }
} 