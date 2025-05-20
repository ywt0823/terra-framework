package com.terra.framework.nova.rag.document.impl;

import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.document.DocumentSplitter;
import com.terra.framework.nova.rag.document.SimpleDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 递归字符分割器
 * 将文档按照字符级别分割成块
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Slf4j
public class RecursiveCharacterSplitter implements DocumentSplitter {

    @Data
    @AllArgsConstructor
    public static class Config implements SplitterConfig {
        private int chunkSize;
        private int overlap;
        
        public static Config of(int chunkSize, int overlap) {
            return new Config(chunkSize, overlap);
        }
    }
    
    private final Config defaultConfig;
    private final String[] separators;
    
    /**
     * 创建分割器
     *
     * @param chunkSize 默认分块大小
     * @param overlap 默认重叠大小
     */
    public RecursiveCharacterSplitter(int chunkSize, int overlap) {
        this(chunkSize, overlap, new String[]{"\n\n", "\n", " ", ""});
    }
    
    /**
     * 创建分割器
     *
     * @param chunkSize 默认分块大小
     * @param overlap 默认重叠大小
     * @param separators 分隔符列表，按照优先级顺序
     */
    public RecursiveCharacterSplitter(int chunkSize, int overlap, String[] separators) {
        this.defaultConfig = new Config(chunkSize, overlap);
        this.separators = separators;
    }
    
    @Override
    public List<Document> split(Document document, SplitterConfig config) {
        String text = document.getContent();
        if (text == null || text.isEmpty()) {
            return List.of();
        }
        
        int chunkSize = config.getChunkSize();
        int overlap = config.getOverlap();
        
        // 如果文本长度小于块大小，直接返回原文档
        if (text.length() <= chunkSize) {
            return List.of(document);
        }
        
        // 递归分割文本
        List<String> chunks = splitText(text, chunkSize, overlap);
        List<Document> result = new ArrayList<>(chunks.size());
        
        // 为每个块创建新文档
        for (int i = 0; i < chunks.size(); i++) {
            String chunk = chunks.get(i);
            Map<String, Object> metadata = new HashMap<>(document.getMetadata());
            
            // 添加分块相关元数据
            metadata.put("chunk_index", i);
            metadata.put("chunk_total", chunks.size());
            metadata.put("parent_document_id", document.getId());
            
            SimpleDocument doc = SimpleDocument.builder()
                    .content(chunk)
                    .metadata(metadata)
                    .build();
            
            result.add(doc);
        }
        
        return result;
    }
    
    @Override
    public List<Document> split(Document document) {
        return split(document, defaultConfig);
    }
    
    /**
     * 分割文本
     *
     * @param text 文本内容
     * @param chunkSize 块大小
     * @param overlap 重叠大小
     * @return 分割后的文本块
     */
    private List<String> splitText(String text, int chunkSize, int overlap) {
        List<String> result = new ArrayList<>();
        
        // 递归分割
        mergeChunks(splitTextRecursive(text, chunkSize, overlap, 0), result);
        
        return result;
    }
    
    /**
     * 递归分割文本
     *
     * @param text 文本内容
     * @param chunkSize 块大小
     * @param overlap 重叠大小
     * @param separatorIndex 当前使用的分隔符索引
     * @return 分割后的文本块
     */
    private List<String> splitTextRecursive(String text, int chunkSize, int overlap, int separatorIndex) {
        // 如果已经尝试了所有分隔符，则按照块大小直接拆分
        if (separatorIndex >= separators.length) {
            return splitOnSize(text, chunkSize, overlap);
        }
        
        String separator = separators[separatorIndex];
        
        // 如果当前分隔符是空字符串，直接按字符分割
        if (separator.isEmpty()) {
            return splitOnSize(text, chunkSize, overlap);
        }
        
        // 按当前分隔符分割
        String[] segments = text.split(separator, -1);
        
        // 如果分割结果只有一段，尝试下一个分隔符
        if (segments.length == 1) {
            return splitTextRecursive(text, chunkSize, overlap, separatorIndex + 1);
        }
        
        List<String> chunks = new ArrayList<>();
        StringBuilder currentChunk = new StringBuilder();
        
        for (int i = 0; i < segments.length; i++) {
            String segment = segments[i];
            
            // 如果不是最后一个片段，添加分隔符
            if (i < segments.length - 1) {
                segment = segment + separator;
            }
            
            // 如果当前块加上新片段小于等于块大小，则添加到当前块
            if (currentChunk.length() + segment.length() <= chunkSize) {
                currentChunk.append(segment);
            } else {
                // 如果当前块非空，添加到结果
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString());
                }
                
                // 如果片段本身就大于块大小，递归处理
                if (segment.length() > chunkSize) {
                    chunks.addAll(splitTextRecursive(segment, chunkSize, overlap, separatorIndex + 1));
                } else {
                    currentChunk = new StringBuilder(segment);
                }
            }
        }
        
        // 添加最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        
        return chunks;
    }
    
    /**
     * 直接按照大小分割文本
     *
     * @param text 文本内容
     * @param chunkSize 块大小
     * @param overlap 重叠大小
     * @return 分割后的文本块
     */
    private List<String> splitOnSize(String text, int chunkSize, int overlap) {
        List<String> chunks = new ArrayList<>();
        
        int start = 0;
        while (start < text.length()) {
            int end = Math.min(start + chunkSize, text.length());
            chunks.add(text.substring(start, end));
            start = end - overlap;
            
            // 防止重叠导致无限循环
            if (start >= end) {
                break;
            }
        }
        
        return chunks;
    }
    
    /**
     * 合并小块到结果列表
     *
     * @param chunks 待合并的块
     * @param result 结果列表
     */
    private void mergeChunks(List<String> chunks, List<String> result) {
        for (String chunk : chunks) {
            if (chunk.trim().isEmpty()) {
                continue;
            }
            result.add(chunk);
        }
    }
} 