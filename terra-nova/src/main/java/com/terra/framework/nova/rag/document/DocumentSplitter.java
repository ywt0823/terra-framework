package com.terra.framework.nova.rag.document;

import java.util.List;

/**
 * 文档分割器接口
 * 用于将大型文档分割成适合嵌入和检索的较小块
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public interface DocumentSplitter extends DocumentProcessor {

    /**
     * 分割器配置
     */
    interface SplitterConfig {
        /**
         * 获取分块大小
         *
         * @return 分块大小
         */
        int getChunkSize();
        
        /**
         * 获取块重叠大小
         *
         * @return 块重叠大小
         */
        int getOverlap();
    }
    
    /**
     * 分割单个文档
     *
     * @param document 待分割文档
     * @param config 分割配置
     * @return 分割后的文档列表
     */
    List<Document> split(Document document, SplitterConfig config);
    
    /**
     * 分割单个文档（使用默认配置）
     *
     * @param document 待分割文档
     * @return 分割后的文档列表
     */
    List<Document> split(Document document);
    
    /**
     * 分割多个文档
     *
     * @param documents 待分割文档列表
     * @param config 分割配置
     * @return 分割后的文档列表
     */
    default List<Document> split(List<Document> documents, SplitterConfig config) {
        return documents.stream()
                .flatMap(doc -> split(doc, config).stream())
                .toList();
    }
    
    /**
     * 实现DocumentProcessor的process方法
     * 使用默认配置分割文档
     *
     * @param documents 待处理文档列表
     * @return 分割后的文档列表
     */
    @Override
    default List<Document> process(List<Document> documents) {
        return documents.stream()
                .flatMap(doc -> split(doc).stream())
                .toList();
    }
} 