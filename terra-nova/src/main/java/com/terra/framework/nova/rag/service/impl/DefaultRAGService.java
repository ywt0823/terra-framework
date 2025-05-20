package com.terra.framework.nova.rag.service.impl;

import com.terra.framework.nova.rag.context.ContextBuilder;
import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.document.DocumentLoader;
import com.terra.framework.nova.rag.document.DocumentSplitter;
import com.terra.framework.nova.rag.embedding.EmbeddingService;
import com.terra.framework.nova.rag.exception.DocumentLoadException;
import com.terra.framework.nova.rag.properties.RAGProperties;
import com.terra.framework.nova.rag.retrieval.RetrievalOptions;
import com.terra.framework.nova.rag.retrieval.Retriever;
import com.terra.framework.nova.rag.retrieval.impl.DefaultRetriever;
import com.terra.framework.nova.rag.service.RAGService;
import com.terra.framework.nova.rag.storage.VectorStore;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * RAG服务默认实现
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Slf4j
public class DefaultRAGService implements RAGService {

    private final Retriever retriever;
    private final EmbeddingService embeddingService;
    private final DocumentSplitter documentSplitter;
    private final ContextBuilder contextBuilder;
    private final RAGProperties properties;
    private final VectorStore vectorStore;
    private DocumentLoader documentLoader;

    /**
     * 创建RAG服务
     *
     * @param retriever 检索器
     * @param embeddingService 嵌入服务
     * @param documentSplitter 文档分割器
     * @param contextBuilder 上下文构建器
     * @param properties RAG配置
     */
    public DefaultRAGService(
            Retriever retriever,
            EmbeddingService embeddingService,
            DocumentSplitter documentSplitter,
            ContextBuilder contextBuilder,
            RAGProperties properties) {
        this.retriever = retriever;
        this.embeddingService = embeddingService;
        this.documentSplitter = documentSplitter;
        this.contextBuilder = contextBuilder;
        this.properties = properties;
        
        // 从retriever中获取vectorStore
        if (retriever instanceof DefaultRetriever) {
            this.vectorStore = ((DefaultRetriever) retriever).getVectorStore();
        } else {
            throw new IllegalArgumentException("检索器必须是DefaultRetriever类型");
        }
    }

    /**
     * 设置文档加载器
     *
     * @param documentLoader 文档加载器
     */
    public void setDocumentLoader(DocumentLoader documentLoader) {
        this.documentLoader = documentLoader;
    }

    @Override
    public boolean addDocument(Document document) {
        if (document == null) {
            log.warn("无法添加空文档");
            return false;
        }

        try {
            // 分割文档
            List<Document> splitDocuments = documentSplitter.split(document);
            
            // 创建嵌入向量
            List<float[]> embeddings = embeddingService.createEmbeddingsForDocuments(splitDocuments);
            
            // 添加到向量存储
            vectorStore.addDocuments(splitDocuments, embeddings);
            
            log.info("成功添加文档: {}, 分割为 {} 个块", document.getId(), splitDocuments.size());
            return true;
        } catch (Exception e) {
            log.error("添加文档失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int addDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return 0;
        }

        int successCount = 0;
        for (Document document : documents) {
            if (addDocument(document)) {
                successCount++;
            }
        }

        log.info("批量添加文档: 成功 {}/{}", successCount, documents.size());
        return successCount;
    }

    @Override
    public int loadDocuments(String source) throws DocumentLoadException {
        if (documentLoader == null) {
            throw new IllegalStateException("DocumentLoader未设置，无法加载文档");
        }

        List<Document> documents = documentLoader.loadDocuments(source);
        return addDocuments(documents);
    }

    @Override
    public int loadDocuments(List<String> sources) throws DocumentLoadException {
        if (documentLoader == null) {
            throw new IllegalStateException("DocumentLoader未设置，无法加载文档");
        }

        List<Document> documents = documentLoader.loadDocuments(sources);
        return addDocuments(documents);
    }

    @Override
    public List<Document> retrieve(String query, int topK) {
        return retriever.retrieve(query, topK);
    }

    @Override
    public List<Document> retrieve(String query, RetrievalOptions options) {
        return retriever.retrieve(query, options);
    }

    @Override
    public String generateContext(String query, int topK) {
        List<Document> documents = retrieve(query, topK);
        return contextBuilder.buildContext(documents, query);
    }

    @Override
    public String generateContext(String query, RetrievalOptions options, Map<String, Object> parameters) {
        List<Document> documents = retrieve(query, options);
        
        if (parameters == null) {
            parameters = new HashMap<>();
        }
        
        return contextBuilder.buildContext(documents, query, parameters);
    }

    @Override
    public boolean removeDocument(String documentId) {
        if (documentId == null || documentId.isEmpty()) {
            return false;
        }

        try {
            vectorStore.deleteDocument(documentId);
            log.info("成功删除文档: {}", documentId);
            return true;
        } catch (Exception e) {
            log.error("删除文档失败: {}", e.getMessage(), e);
            return false;
        }
    }

    @Override
    public int removeDocuments(List<String> documentIds) {
        if (documentIds == null || documentIds.isEmpty()) {
            return 0;
        }

        try {
            vectorStore.deleteDocuments(documentIds);
            log.info("成功批量删除文档, 数量: {}", documentIds.size());
            return documentIds.size();
        } catch (Exception e) {
            log.error("批量删除文档失败: {}", e.getMessage(), e);
            return 0;
        }
    }

    @Override
    public void clearAll() {
        try {
            vectorStore.clear();
            log.info("成功清空知识库");
        } catch (Exception e) {
            log.error("清空知识库失败: {}", e.getMessage(), e);
        }
    }
} 