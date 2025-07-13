package com.terra.framework.nova.prompt.template;

import com.terra.framework.nova.prompt.parser.PromptXmlParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * A registry that holds all parsed prompt templates from XML files.
 * <p>
 * This class is responsible for loading prompt resources from a specified location,
 * parsing them, and storing them in a thread-safe map for fast runtime access.
 *
 * @author DeavyJones
 */
public class PromptTemplateRegistry {

    private static final Logger log = LoggerFactory.getLogger(PromptTemplateRegistry.class);

    private final Map<String, PromptTemplate> knownTemplates = new ConcurrentHashMap<>();

    /**
     * Loads and parses prompt templates from the given locations.
     *
     * @param mapperLocations A String array of resource locations (e.g., "prompts/").
     */
    public void loadTemplates(String[] mapperLocations) {
        if (mapperLocations == null || mapperLocations.length == 0) {
            return;
        }

        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        for (String mapperLocation : mapperLocations) {
            try {
                // 简化实现，直接扫描指定目录下的XML文件
                Enumeration<URL> resources = classLoader.getResources(mapperLocation);
                while (resources.hasMoreElements()) {
                    URL resource = resources.nextElement();
                    loadFromUrl(resource);
                }
            } catch (IOException e) {
                log.warn("Could not resolve prompt mapper resource: {}", mapperLocation, e);
            }
        }
    }

    private void loadFromUrl(URL resource) {
        try (InputStream inputStream = resource.openStream()) {
            log.debug("Loading prompt definitions from XML file: {}", resource.getPath());
            PromptXmlParser parser = new PromptXmlParser(inputStream, resource.getPath());
            parser.parse().forEach(this::addPromptTemplate);
        } catch (IOException e) {
            log.warn("Failed to load prompt template from: {}", resource.getPath(), e);
        }
    }

    /**
     * Retrieves a prompt template by its fully qualified ID.
     *
     * @param fqId The fully qualified ID (e.g., "com.example.UserPrompt.findById").
     * @return The {@link PromptTemplate}, or null if not found.
     */
    public PromptTemplate getTemplate(String fqId) {
        return knownTemplates.get(fqId);
    }

    /**
     * Adds a parsed prompt template to the registry.
     *
     * @param fqId     The fully qualified ID of the prompt.
     * @param template The prompt template object.
     */
    public void addPromptTemplate(String fqId, PromptTemplate template) {
        if (knownTemplates.containsKey(fqId)) {
            log.warn("Overwriting existing prompt template with ID: {}", fqId);
        }
        knownTemplates.put(fqId, template);
    }
} 