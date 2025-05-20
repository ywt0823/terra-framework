package com.terra.framework.nova.rag.document;

import com.terra.framework.nova.rag.exception.DocumentLoadException;

import java.util.List;

/**
 * 文档加载器接口
 * 负责从各种来源加载文档
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public interface DocumentLoader {
    
    /**
     * 从指定来源加载文档
     *
     * @param source 文档来源，可以是文件路径、URL等
     * @return 加载的文档列表
     * @throws DocumentLoadException 加载异常
     */
    List<Document> loadDocuments(String source) throws DocumentLoadException;
    
    /**
     * 从多个来源加载文档
     *
     * @param sources 多个文档来源
     * @return 加载的文档列表
     * @throws DocumentLoadException 加载异常
     */
    default List<Document> loadDocuments(List<String> sources) throws DocumentLoadException {
        return sources.stream()
                .flatMap(source -> {
                    try {
                        return loadDocuments(source).stream();
                    } catch (DocumentLoadException e) {
                        throw new RuntimeException(e);
                    }
                })
                .toList();
    }
} 