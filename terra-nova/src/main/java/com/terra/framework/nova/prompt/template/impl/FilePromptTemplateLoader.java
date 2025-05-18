package com.terra.framework.nova.prompt.template.impl;

import com.terra.framework.nova.prompt.template.PromptTemplate;
import com.terra.framework.nova.prompt.template.PromptTemplateLoader;
import com.terra.framework.nova.prompt.template.TemplateEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.util.FileCopyUtils;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 基于文件系统的提示词模板加载器
 *
 * @author terra-nova
 */
public class FilePromptTemplateLoader implements PromptTemplateLoader {

    private static final Logger log = LoggerFactory.getLogger(FilePromptTemplateLoader.class);

    private final String basePath;
    private final TemplateEngine engine;
    private final ResourceLoader resourceLoader;
    private final String fileExtension;
    private final Map<String, PromptTemplate> templateCache = new ConcurrentHashMap<>();

    /**
     * 构造函数
     *
     * @param basePath 模板基础路径
     * @param engine 模板引擎
     * @param resourceLoader 资源加载器
     */
    public FilePromptTemplateLoader(String basePath, TemplateEngine engine, ResourceLoader resourceLoader) {
        this(basePath, engine, resourceLoader, ".txt");
    }

    /**
     * 构造函数
     *
     * @param basePath 模板基础路径
     * @param engine 模板引擎
     * @param resourceLoader 资源加载器
     * @param fileExtension 文件扩展名（必须包含点号，例如 ".prompt"）
     */
    public FilePromptTemplateLoader(String basePath, TemplateEngine engine, ResourceLoader resourceLoader, String fileExtension) {
        this.basePath = basePath.endsWith("/") ? basePath : basePath + "/";
        this.engine = engine;
        this.resourceLoader = resourceLoader;
        this.fileExtension = fileExtension != null && !fileExtension.isEmpty() ? fileExtension : ".txt";
    }

    @Override
    public PromptTemplate load(String templateId) {
        return templateCache.computeIfAbsent(templateId, this::loadTemplate);
    }

    /**
     * 加载模板
     *
     * @param templateId 模板ID
     * @return 模板
     */
    private PromptTemplate loadTemplate(String templateId) {
        String templatePath = basePath + templateId + fileExtension;
        try {
            Resource resource = resourceLoader.getResource(templatePath);
            if (!resource.exists()) {
                log.error("Template not found: {}", templatePath);
                throw new IllegalArgumentException("Template not found: " + templatePath);
            }

            String template = readResourceContent(resource);
            return new StringPromptTemplate(template, engine);
        } catch (IOException e) {
            log.error("Failed to load template: {}", templatePath, e);
            throw new RuntimeException("Failed to load template: " + templatePath, e);
        }
    }

    /**
     * 读取资源内容
     *
     * @param resource 资源
     * @return 资源内容
     * @throws IOException 读取异常
     */
    private String readResourceContent(Resource resource) throws IOException {
        try (Reader reader = new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8)) {
            return FileCopyUtils.copyToString(reader);
        }
    }

    /**
     * 清除模板缓存
     */
    public void clearCache() {
        templateCache.clear();
    }
}
