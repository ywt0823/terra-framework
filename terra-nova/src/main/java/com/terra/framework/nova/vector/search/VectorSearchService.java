package com.terra.framework.nova.vector.search;

import com.terra.framework.nova.vector.document.Document;

import java.util.List;
import java.util.Map;

/**
 * 向量搜索服务接口
 *
 * @author terra-nova
 */
public interface VectorSearchService {
    
    /**
     * 根据文本查询相似文档
     *
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @return 搜索结果列表
     */
    List<SearchResult> search(String query, int limit);
    
    /**
     * 根据文本查询相似文档，并指定相似度阈值
     *
     * @param query 查询文本
     * @param limit 返回结果数量限制
     * @param threshold 相似度阈值
     * @return 搜索结果列表
     */
    List<SearchResult> search(String query, int limit, float threshold);
    
    /**
     * 根据文本和过滤器查询相似文档
     *
     * @param query 查询文本
     * @param filters 元数据过滤条件
     * @param limit 返回结果数量限制
     * @return 搜索结果列表
     */
    List<SearchResult> searchWithFilters(String query, Map<String, Object> filters, int limit);
    
    /**
     * 保存文档到向量存储
     *
     * @param document 文档
     * @return 是否保存成功
     */
    boolean save(Document document);
    
    /**
     * 批量保存文档到向量存储
     *
     * @param documents 文档列表
     * @return 保存成功的文档数量
     */
    int saveAll(List<Document> documents);
    
    /**
     * 从向量存储中删除文档
     *
     * @param documentId 文档ID
     * @return 是否删除成功
     */
    boolean delete(String documentId);
    
    /**
     * 清空向量存储
     */
    void clear();
    
    /**
     * 获取向量存储中的文档数量
     *
     * @return 文档数量
     */
    long count();
} 