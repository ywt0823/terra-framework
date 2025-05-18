package com.terra.framework.nova.prompt.template;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 提示词模板注册表，管理模板和加载器
 *
 * @author terra-nova
 */
public class PromptTemplateRegistry {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateRegistry.class);

    private final Map<String, PromptTemplate> templates = new ConcurrentHashMap<>();
    private final List<PromptTemplateLoader> loaders = new ArrayList<>();

    /**
     * 注册模板加载器
     *
     * @param loader 模板加载器
     */
    public void registerLoader(PromptTemplateLoader loader) {
        loaders.add(loader);
        log.info("Registered prompt template loader: {}", loader.getClass().getName());
    }

    /**
     * 注册模板
     *
     * @param templateId 模板ID
     * @param template 模板
     */
    public void registerTemplate(String templateId, PromptTemplate template) {
        templates.put(templateId, template);
        log.info("Registered prompt template: {}", templateId);
    }

    /**
     * 获取模板
     *
     * @param templateId 模板ID
     * @return 模板
     */
    public PromptTemplate getTemplate(String templateId) {
        // 首先从已注册的模板中查找
        PromptTemplate template = templates.get(templateId);
        if (template != null) {
            return template;
        }

        // 从加载器中加载
        for (PromptTemplateLoader loader : loaders) {
            try {
                template = loader.load(templateId);
                if (template != null) {
                    // 缓存加载的模板
                    templates.put(templateId, template);
                    return template;
                }
            } catch (Exception e) {
                log.debug("Failed to load template {} from loader {}: {}",
                        templateId, loader.getClass().getName(), e.getMessage());
            }
        }

        throw new IllegalArgumentException("Template not found: " + templateId);
    }

    /**
     * 清除注册的模板
     */
    public void clearTemplates() {
        templates.clear();
        log.info("Cleared all registered templates");
    }

    /**
     * 清除注册的加载器
     */
    public void clearLoaders() {
        loaders.clear();
        log.info("Cleared all registered loaders");
    }
}
