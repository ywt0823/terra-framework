package com.terra.framework.nova.rag.embedding;

import com.terra.framework.nova.rag.document.Document;

import java.util.List;
import java.util.Map;

/**
 * 嵌入服务
 * 管理嵌入模型并提供嵌入功能
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public interface EmbeddingService {
    
    /**
     * 为文本创建嵌入向量
     *
     * @param text 文本
     * @return 嵌入向量
     */
    float[] createEmbedding(String text);
    
    /**
     * 为文本批量创建嵌入向量
     *
     * @param texts 文本列表
     * @return 嵌入向量列表
     */
    List<float[]> createEmbeddings(List<String> texts);
    
    /**
     * 为文档创建嵌入向量
     *
     * @param document 文档
     * @return 嵌入向量
     */
    default float[] createEmbeddingForDocument(Document document) {
        return createEmbedding(document.getContent());
    }
    
    /**
     * 为文档批量创建嵌入向量
     *
     * @param documents 文档列表
     * @return 嵌入向量列表
     */
    default List<float[]> createEmbeddingsForDocuments(List<Document> documents) {
        return createEmbeddings(documents.stream()
                .map(Document::getContent)
                .toList());
    }
    
    /**
     * 使用特定模型为文本创建嵌入向量
     *
     * @param text 文本
     * @param modelId 模型ID
     * @return 嵌入向量
     */
    float[] createEmbedding(String text, String modelId);
    
    /**
     * 使用特定模型为文本批量创建嵌入向量
     *
     * @param texts 文本列表
     * @param modelId 模型ID
     * @return 嵌入向量列表
     */
    List<float[]> createEmbeddings(List<String> texts, String modelId);
    
    /**
     * 获取当前使用的嵌入模型
     *
     * @return 嵌入模型
     */
    EmbeddingModel getEmbeddingModel();
    
    /**
     * 获取特定的嵌入模型
     *
     * @param modelId 模型ID
     * @return 嵌入模型
     */
    EmbeddingModel getEmbeddingModel(String modelId);
    
    /**
     * 获取所有可用的嵌入模型
     *
     * @return 嵌入模型Map，键为模型ID
     */
    Map<String, EmbeddingModel> getAvailableModels();
} 