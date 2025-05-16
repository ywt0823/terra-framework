package com.terra.framework.nova.vector.redis;

import com.terra.framework.nova.vector.document.Document;
import com.terra.framework.nova.vector.document.DocumentProcessor;
import com.terra.framework.nova.vector.embedding.EmbeddingService;
import com.terra.framework.nova.vector.search.SearchResult;
import com.terra.framework.nova.vector.search.VectorSearchService;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

/**
 * Redis向量存储服务实现
 *
 * @author terra-nova
 */
@Slf4j
public class RedisVectorStore implements VectorSearchService {
    
    private final RedisVectorClient redisClient;
    private final EmbeddingService embeddingService;
    private final DocumentProcessor documentProcessor;
    private final float defaultThreshold;
    
    /**
     * 构造函数
     *
     * @param redisClient Redis向量客户端
     * @param embeddingService 嵌入服务
     * @param documentProcessor 文档处理器
     */
    public RedisVectorStore(RedisVectorClient redisClient, EmbeddingService embeddingService, DocumentProcessor documentProcessor) {
        this(redisClient, embeddingService, documentProcessor, 0.7f);
    }
    
    /**
     * 构造函数
     *
     * @param redisClient Redis向量客户端
     * @param embeddingService 嵌入服务
     * @param documentProcessor 文档处理器
     * @param defaultThreshold 默认相似度阈值
     */
    public RedisVectorStore(RedisVectorClient redisClient, EmbeddingService embeddingService, 
                           DocumentProcessor documentProcessor, float defaultThreshold) {
        this.redisClient = redisClient;
        this.embeddingService = embeddingService;
        this.documentProcessor = documentProcessor;
        this.defaultThreshold = defaultThreshold;
    }
    
    @Override
    public List<SearchResult> search(String query, int limit) {
        return search(query, limit, defaultThreshold);
    }
    
    @Override
    public List<SearchResult> search(String query, int limit, float threshold) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // 生成查询向量
        float[] queryVector = embeddingService.embed(query);
        
        // 获取所有文档ID
        Set<String> allDocIds = redisClient.getAllDocumentIds();
        if (allDocIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 计算相似度并排序
        return findSimilarDocuments(allDocIds, queryVector, limit, threshold);
    }
    
    @Override
    public List<SearchResult> searchWithFilters(String query, Map<String, Object> filters, int limit) {
        if (query == null || query.trim().isEmpty()) {
            return Collections.emptyList();
        }
        
        // 生成查询向量
        float[] queryVector = embeddingService.embed(query);
        
        // 获取符合过滤条件的文档ID
        Set<String> filteredDocIds = redisClient.filterByMetadata(filters);
        if (filteredDocIds.isEmpty()) {
            return Collections.emptyList();
        }
        
        // 计算相似度并排序
        return findSimilarDocuments(filteredDocIds, queryVector, limit, defaultThreshold);
    }
    
    /**
     * 找到与查询向量相似的文档
     *
     * @param docIds 文档ID集合
     * @param queryVector 查询向量
     * @param limit 结果数量限制
     * @param threshold 相似度阈值
     * @return 搜索结果列表
     */
    private List<SearchResult> findSimilarDocuments(Set<String> docIds, float[] queryVector, int limit, float threshold) {
        List<SearchResult> results = new ArrayList<>();
        
        for (String docId : docIds) {
            // 获取文档向量
            float[] docVector = redisClient.getVector(docId);
            if (docVector == null) {
                continue;
            }
            
            // 计算相似度
            float similarity = embeddingService.cosineSimilarity(queryVector, docVector);
            
            // 如果相似度超过阈值，添加到结果集
            if (similarity >= threshold) {
                // 获取元数据
                Map<String, Object> metadata = redisClient.getMetadata(docId);
                
                // 从元数据中提取文档内容和标题
                String content = metadata != null && metadata.containsKey("content") 
                        ? metadata.get("content").toString() : "";
                String title = metadata != null && metadata.containsKey("title") 
                        ? metadata.get("title").toString() : null;
                
                // 创建文档和搜索结果
                Document document = Document.builder()
                        .id(docId)
                        .content(content)
                        .title(title)
                        .metadata(metadata != null ? metadata : new HashMap<>())
                        .build();
                
                results.add(SearchResult.of(document, similarity));
            }
        }
        
        // 按相似度降序排序
        results.sort(Comparator.comparing(SearchResult::getScore).reversed());
        
        // 限制结果数量
        return results.stream()
                .limit(limit)
                .collect(Collectors.toList());
    }
    
    @Override
    public boolean save(Document document) {
        if (document == null || document.getContent() == null) {
            return false;
        }
        
        try {
            // 生成文档向量
            float[] vector = embeddingService.embed(document.getContent());
            
            // 准备元数据，确保包含内容和标题
            Map<String, Object> metadata = new HashMap<>(document.getMetadata());
            metadata.put("content", document.getContent());
            if (document.getTitle() != null) {
                metadata.put("title", document.getTitle());
            }
            
            // 存储到Redis
            return redisClient.storeVector(document.getId(), vector, metadata);
        } catch (Exception e) {
            log.error("保存文档失败: {}", document.getId(), e);
            return false;
        }
    }
    
    @Override
    public int saveAll(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0;
        }
        
        try {
            // 预处理文档（切分大文档）
            List<Document> processedDocuments = new ArrayList<>();
            for (Document doc : documents) {
                processedDocuments.addAll(documentProcessor.splitDocument(doc));
            }
            
            // 提取文本内容
            List<String> texts = processedDocuments.stream()
                    .map(Document::getContent)
                    .collect(Collectors.toList());
            
            // 批量生成向量
            CompletableFuture<List<float[]>> vectorsFuture = embeddingService.embedBatchAsync(texts);
            
            // 等待向量生成完成
            List<float[]> vectors = vectorsFuture.join();
            
            // 准备元数据
            for (Document doc : processedDocuments) {
                Map<String, Object> metadata = new HashMap<>(doc.getMetadata());
                metadata.put("content", doc.getContent());
                if (doc.getTitle() != null) {
                    metadata.put("title", doc.getTitle());
                }
                doc.setMetadata(metadata);
            }
            
            // 批量存储
            return redisClient.storeVectors(processedDocuments, vectors);
        } catch (Exception e) {
            log.error("批量保存文档失败", e);
            return 0;
        }
    }
    
    @Override
    public boolean delete(String documentId) {
        if (documentId == null) {
            return false;
        }
        
        return redisClient.deleteVector(documentId);
    }
    
    @Override
    public void clear() {
        redisClient.clear();
    }
    
    @Override
    public long count() {
        return redisClient.count();
    }
} 