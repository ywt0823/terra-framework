package com.terra.framework.nova.rag.embedding;

import java.util.List;

/**
 * 嵌入模型接口
 * 将文本转换为向量
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public interface EmbeddingModel {
    
    /**
     * 获取嵌入模型ID
     *
     * @return 模型ID
     */
    String getModelId();
    
    /**
     * 获取嵌入向量维度
     *
     * @return 向量维度
     */
    int getDimension();
    
    /**
     * 为单个文本生成嵌入向量
     *
     * @param text 文本
     * @return 嵌入向量
     */
    float[] embed(String text);
    
    /**
     * 为多个文本批量生成嵌入向量
     *
     * @param texts 文本列表
     * @return 嵌入向量列表
     */
    List<float[]> embed(List<String> texts);
} 