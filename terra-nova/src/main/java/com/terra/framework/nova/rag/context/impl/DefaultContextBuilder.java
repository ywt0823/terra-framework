package com.terra.framework.nova.rag.context.impl;

import com.terra.framework.nova.rag.context.ContextBuilder;
import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.properties.RAGProperties;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * 默认上下文构建器实现
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Slf4j
public class DefaultContextBuilder implements ContextBuilder {
    
    private final RAGProperties.Context config;
    
    /**
     * 创建默认上下文构建器
     *
     * @param properties RAG配置
     */
    public DefaultContextBuilder(RAGProperties properties) {
        this.config = properties.getContext();
    }
    
    @Override
    public String buildContext(List<Document> documents, String query) {
        return buildContext(documents, query, new HashMap<>());
    }
    
    @Override
    public String buildContext(List<Document> documents, String query, Map<String, Object> parameters) {
        if (documents == null || documents.isEmpty()) {
            return "";
        }
        
        // 格式化文档
        String formattedDocs = formatDocuments(documents);
        
        // 填充模板
        String template = config.getTemplate();
        
        // 替换模板中的变量
        String result = template
                .replace("{context}", formattedDocs)
                .replace("{question}", query);
        
        // 替换自定义参数
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            if (entry.getValue() != null) {
                result = result.replace("{" + entry.getKey() + "}", entry.getValue().toString());
            }
        }
        
        return result;
    }
    
    @Override
    public String formatDocuments(List<Document> documents) {
        if (documents == null || documents.isEmpty()) {
            return "";
        }
        
        // 如果不需要格式化，则直接拼接文档内容
        if (!config.isFormatDocuments()) {
            return documents.stream()
                    .map(Document::getContent)
                    .collect(Collectors.joining("\n\n"));
        }
        
        // 使用模板格式化每个文档
        String documentTemplate = config.getDocumentTemplate();
        
        return IntStream.range(0, documents.size())
                .mapToObj(i -> {
                    Document doc = documents.get(i);
                    String content = doc.getContent();
                    
                    // 获取来源信息（如果有）
                    String source = getSourceInfo(doc);
                    
                    // 替换模板变量
                    return documentTemplate
                            .replace("{index}", String.valueOf(i + 1))
                            .replace("{content}", content)
                            .replace("{source}", source);
                })
                .collect(Collectors.joining("\n\n"));
    }
    
    /**
     * 获取文档来源信息
     *
     * @param document 文档
     * @return 来源信息
     */
    private String getSourceInfo(Document document) {
        Map<String, Object> metadata = document.getMetadata();
        if (metadata == null) {
            return "未知来源";
        }
        
        // 尝试获取source字段
        if (metadata.containsKey("source")) {
            return metadata.get("source").toString();
        }
        
        // 尝试获取其他可能表示来源的字段
        for (String key : new String[] {"url", "file", "path", "filename"}) {
            if (metadata.containsKey(key)) {
                return metadata.get(key).toString();
            }
        }
        
        // 如果有parent_document_id，则标记为子文档
        if (metadata.containsKey("parent_document_id")) {
            return "文档ID: " + metadata.get("parent_document_id");
        }
        
        return "文档ID: " + document.getId();
    }
} 