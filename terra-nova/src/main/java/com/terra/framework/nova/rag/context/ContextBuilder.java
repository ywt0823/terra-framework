package com.terra.framework.nova.rag.context;

import com.terra.framework.nova.rag.document.Document;

import java.util.List;
import java.util.Map;

/**
 * 上下文构建器接口
 * 负责将检索到的文档组织成适合LLM使用的上下文格式
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
public interface ContextBuilder {
    
    /**
     * 构建上下文
     *
     * @param documents 检索到的文档
     * @param query 原始查询
     * @return 构建好的上下文
     */
    String buildContext(List<Document> documents, String query);
    
    /**
     * 构建上下文
     *
     * @param documents 检索到的文档
     * @param query 原始查询
     * @param parameters 额外参数
     * @return 构建好的上下文
     */
    String buildContext(List<Document> documents, String query, Map<String, Object> parameters);
    
    /**
     * 构建上下文（不包含模板包装，仅格式化文档内容）
     *
     * @param documents 检索到的文档
     * @return 格式化后的文档内容
     */
    String formatDocuments(List<Document> documents);
} 