package com.terra.framework.nova.vector.embedding;

import java.util.List;
import java.util.concurrent.CompletableFuture;

/**
 * 嵌入服务接口，负责生成文本的向量表示
 *
 * @author terra-nova
 */
public interface EmbeddingService {
    
    /**
     * 为单个文本生成嵌入向量
     *
     * @param text 文本
     * @return 嵌入向量
     */
    float[] embed(String text);
    
    /**
     * 批量生成嵌入向量
     *
     * @param texts 文本列表
     * @return 嵌入向量列表
     */
    List<float[]> embedBatch(List<String> texts);
    
    /**
     * 异步批量生成嵌入向量
     *
     * @param texts 文本列表
     * @return 嵌入向量列表的异步结果
     */
    CompletableFuture<List<float[]>> embedBatchAsync(List<String> texts);
    
    /**
     * 获取嵌入向量的维度
     *
     * @return 向量维度
     */
    int getDimension();
    
    /**
     * 计算两个向量间的余弦相似度
     *
     * @param vector1 向量1
     * @param vector2 向量2
     * @return 余弦相似度
     */
    default float cosineSimilarity(float[] vector1, float[] vector2) {
        if (vector1.length != vector2.length) {
            throw new IllegalArgumentException("向量维度不匹配");
        }
        
        float dotProduct = 0.0f;
        float norm1 = 0.0f;
        float norm2 = 0.0f;
        
        for (int i = 0; i < vector1.length; i++) {
            dotProduct += vector1[i] * vector2[i];
            norm1 += vector1[i] * vector1[i];
            norm2 += vector2[i] * vector2[i];
        }
        
        norm1 = (float) Math.sqrt(norm1);
        norm2 = (float) Math.sqrt(norm2);
        
        if (norm1 == 0 || norm2 == 0) {
            return 0.0f;
        }
        
        return dotProduct / (norm1 * norm2);
    }
} 