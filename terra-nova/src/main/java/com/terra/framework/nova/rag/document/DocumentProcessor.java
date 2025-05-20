package com.terra.framework.nova.rag.document;

import java.util.List;

/**
 * 文档处理器接口
 * 用于对文档进行处理，如文本清洗、格式转换等
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public interface DocumentProcessor {
    
    /**
     * 处理文档
     *
     * @param documents 待处理文档列表
     * @return 处理后的文档列表
     */
    List<Document> process(List<Document> documents);
    
    /**
     * 处理单个文档
     *
     * @param document 待处理文档
     * @return 处理后的文档列表（一个输入文档可能产生多个输出文档）
     */
    default List<Document> process(Document document) {
        return process(List.of(document));
    }
    
    /**
     * 将多个处理器串联成一个处理管道
     *
     * @param next 下一个处理器
     * @return 组合的处理器
     */
    default DocumentProcessor then(DocumentProcessor next) {
        if (next == null) {
            return this;
        }
        return documents -> next.process(process(documents));
    }
} 