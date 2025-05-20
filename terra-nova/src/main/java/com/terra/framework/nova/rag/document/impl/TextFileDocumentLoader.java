package com.terra.framework.nova.rag.document.impl;

import com.terra.framework.nova.rag.document.Document;
import com.terra.framework.nova.rag.document.DocumentLoader;
import com.terra.framework.nova.rag.document.SimpleDocument;
import com.terra.framework.nova.rag.exception.DocumentLoadException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 文本文件加载器
 * 从文件系统加载文本文件
 *
 * @author Terra Framework Team
 * @date 2025年6月1日
 */
@Slf4j
public class TextFileDocumentLoader implements DocumentLoader {

    private final String[] allowedExtensions;

    /**
     * 创建文本文件加载器
     */
    public TextFileDocumentLoader() {
        this(new String[] {".txt", ".md", ".csv", ".json", ".html", ".xml"});
    }

    /**
     * 创建文本文件加载器，指定允许的文件扩展名
     *
     * @param allowedExtensions 允许的文件扩展名
     */
    public TextFileDocumentLoader(String[] allowedExtensions) {
        this.allowedExtensions = allowedExtensions;
    }

    @Override
    public List<Document> loadDocuments(String source) throws DocumentLoadException {
        Path path = Paths.get(source);
        
        if (!Files.exists(path)) {
            throw new DocumentLoadException("文件不存在: " + source);
        }
        
        if (Files.isDirectory(path)) {
            return loadDirectory(path);
        } else {
            return loadFile(path);
        }
    }

    /**
     * 加载目录中的所有文本文件
     *
     * @param directory 目录路径
     * @return 加载的文档列表
     * @throws DocumentLoadException 加载异常
     */
    private List<Document> loadDirectory(Path directory) throws DocumentLoadException {
        try (Stream<Path> paths = Files.walk(directory)) {
            return paths
                    .filter(Files::isRegularFile)
                    .filter(this::isAllowedFile)
                    .flatMap(file -> {
                        try {
                            return loadFile(file).stream();
                        } catch (DocumentLoadException e) {
                            log.warn("加载文件失败: {}, 原因: {}", file, e.getMessage());
                            return Stream.empty();
                        }
                    })
                    .collect(Collectors.toList());
        } catch (IOException e) {
            throw new DocumentLoadException("加载目录失败: " + directory, e);
        }
    }

    /**
     * 加载单个文本文件
     *
     * @param file 文件路径
     * @return 加载的文档列表
     * @throws DocumentLoadException 加载异常
     */
    private List<Document> loadFile(Path file) throws DocumentLoadException {
        if (!isAllowedFile(file)) {
            throw new DocumentLoadException("不支持的文件类型: " + file);
        }
        
        try {
            String content = Files.readString(file);
            
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", file.toString());
            metadata.put("filename", file.getFileName().toString());
            metadata.put("file_extension", getFileExtension(file));
            metadata.put("file_size", Files.size(file));
            metadata.put("created_time", Files.getLastModifiedTime(file).toMillis());
            
            Document document = SimpleDocument.builder()
                    .content(content)
                    .metadata(metadata)
                    .build();
            
            return List.of(document);
        } catch (IOException e) {
            throw new DocumentLoadException("读取文件失败: " + file, e);
        }
    }

    /**
     * 判断文件是否为允许的类型
     *
     * @param file 文件路径
     * @return 是否为允许的类型
     */
    private boolean isAllowedFile(Path file) {
        String extension = getFileExtension(file);
        
        for (String allowedExt : allowedExtensions) {
            if (extension.equalsIgnoreCase(allowedExt)) {
                return true;
            }
        }
        
        return false;
    }

    /**
     * 获取文件扩展名
     *
     * @param file 文件路径
     * @return 扩展名
     */
    private String getFileExtension(Path file) {
        String filename = file.getFileName().toString();
        int lastDotIndex = filename.lastIndexOf('.');
        
        if (lastDotIndex > 0) {
            return filename.substring(lastDotIndex);
        }
        
        return "";
    }
} 