package com.terra.framework.nova.vector.document;

import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * 文档处理器，负责文档的切分和预处理
 *
 * @author terra-nova
 */
@Slf4j
public class DocumentProcessor {
    
    private final int chunkSize;
    private final int chunkOverlap;
    private final Pattern splitPattern;
    
    /**
     * 构造函数
     *
     * @param chunkSize 块大小（以字符为单位）
     * @param chunkOverlap 块重叠度（以字符为单位）
     */
    public DocumentProcessor(int chunkSize, int chunkOverlap) {
        this.chunkSize = Math.max(100, chunkSize);
        this.chunkOverlap = Math.min(chunkOverlap, chunkSize / 2);
        this.splitPattern = Pattern.compile("[.!?。！？]");
    }
    
    /**
     * 将文档切分为多个小块
     *
     * @param document 原始文档
     * @return 切分后的文档列表
     */
    public List<Document> splitDocument(Document document) {
        String content = document.getContent();
        if (content == null || content.length() <= chunkSize) {
            return List.of(document);
        }
        
        List<Document> chunks = new ArrayList<>();
        List<String> textChunks = splitText(content);
        
        for (int i = 0; i < textChunks.size(); i++) {
            String chunkContent = textChunks.get(i);
            String chunkId = document.getId() + "-chunk-" + i;
            
            Document chunk = Document.builder()
                    .id(chunkId)
                    .content(chunkContent)
                    .title(document.getTitle() != null ? document.getTitle() + " (Part " + (i + 1) + ")" : null)
                    .metadata(new java.util.HashMap<>(document.getMetadata()))
                    .build();
            
            // 添加块索引元数据
            chunk.addMetadata("chunkIndex", i);
            chunk.addMetadata("parentId", document.getId());
            chunk.addMetadata("totalChunks", textChunks.size());
            
            chunks.add(chunk);
        }
        
        log.debug("文档 {} 被切分为 {} 个块", document.getId(), chunks.size());
        return chunks;
    }
    
    /**
     * 将文本按照句子边界切分为多个块
     *
     * @param text 原始文本
     * @return 切分后的文本列表
     */
    private List<String> splitText(String text) {
        List<String> chunks = new ArrayList<>();
        List<String> sentences = splitIntoSentences(text);
        
        StringBuilder currentChunk = new StringBuilder();
        int currentSize = 0;
        
        for (String sentence : sentences) {
            if (currentSize + sentence.length() <= chunkSize || currentSize == 0) {
                // 如果当前块加上新句子不超过chunkSize，或者当前块为空（句子太长的情况）
                currentChunk.append(sentence);
                currentSize += sentence.length();
            } else {
                // 当前块已满，创建新块
                chunks.add(currentChunk.toString());
                
                // 计算重叠部分
                if (chunkOverlap > 0) {
                    String overlap = getOverlapText(currentChunk.toString(), chunkOverlap);
                    currentChunk = new StringBuilder(overlap);
                    currentSize = overlap.length();
                } else {
                    currentChunk = new StringBuilder();
                    currentSize = 0;
                }
                
                // 添加当前句子到新块
                currentChunk.append(sentence);
                currentSize += sentence.length();
            }
        }
        
        // 添加最后一个块
        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString());
        }
        
        return chunks;
    }
    
    /**
     * 将文本切分为句子
     *
     * @param text 原始文本
     * @return 句子列表
     */
    private List<String> splitIntoSentences(String text) {
        List<String> sentences = new ArrayList<>();
        String[] rawSentences = splitPattern.split(text);
        
        for (String sentence : rawSentences) {
            sentence = sentence.trim();
            if (!sentence.isEmpty()) {
                sentences.add(sentence + ". ");
            }
        }
        
        return sentences;
    }
    
    /**
     * 获取文本的结尾部分作为重叠部分
     *
     * @param text 原始文本
     * @param overlapSize 重叠大小
     * @return 重叠文本
     */
    private String getOverlapText(String text, int overlapSize) {
        if (text.length() <= overlapSize) {
            return text;
        }
        return text.substring(text.length() - overlapSize);
    }
    
    /**
     * 处理一个文本输入，创建文档
     *
     * @param text 文本内容
     * @return 文档
     */
    public Document createDocument(String text) {
        return Document.of(UUID.randomUUID().toString(), text);
    }
    
    /**
     * 处理一个带标题的文本输入，创建文档
     *
     * @param title 标题
     * @param text 文本内容
     * @return 文档
     */
    public Document createDocument(String title, String text) {
        return Document.of(UUID.randomUUID().toString(), title, text);
    }
} 