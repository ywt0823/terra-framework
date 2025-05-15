package com.terra.framework.nova.prompt.template.impl;

import com.terra.framework.nova.prompt.template.PromptTemplate;
import com.terra.framework.nova.prompt.template.TemplateMetadata;
import com.terra.framework.nova.core.exception.NovaException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DefaultPromptTemplate implements PromptTemplate {
    private final Map<String, Map<String, String>> templateStore = new ConcurrentHashMap<>();
    private final Map<String, TemplateMetadata> metadataStore = new ConcurrentHashMap<>();
    private final Pattern variablePattern = Pattern.compile("\\{\\{\\s*(\\w+)\\s*\\}\\}");

    @Override
    public String render(Map<String, Object> variables) {
        if (variables == null || variables.isEmpty()) {
            throw new NovaException("Variables cannot be null or empty");
        }

        String templateId = (String) variables.get("templateId");
        if (templateId == null) {
            throw new NovaException("Template ID must be provided in variables");
        }

        String version = (String) variables.getOrDefault("version", "latest");
        String template = load(templateId, version);

        return renderTemplate(template, variables);
    }

    @Override
    public void save(String templateId, String content) {
        if (templateId == null || content == null) {
            throw new NovaException("Template ID and content cannot be null");
        }

        // Create version map if it doesn't exist
        Map<String, String> versions = templateStore.computeIfAbsent(templateId, k -> new ConcurrentHashMap<>());
        
        // Generate version number
        String version = generateVersion(templateId);
        
        // Save template content
        versions.put(version, content);
        
        // Update metadata
        updateMetadata(templateId, version);
    }

    @Override
    public String load(String templateId, String version) {
        if (templateId == null) {
            throw new NovaException("Template ID cannot be null");
        }

        Map<String, String> versions = templateStore.get(templateId);
        if (versions == null) {
            throw new NovaException("Template not found: " + templateId);
        }

        String templateContent;
        if (version == null || version.equals("latest")) {
            templateContent = getLatestVersion(versions);
        } else {
            templateContent = versions.get(version);
        }

        if (templateContent == null) {
            throw new NovaException("Template version not found: " + version);
        }

        return templateContent;
    }

    @Override
    public TemplateMetadata getMetadata(String templateId) {
        if (templateId == null) {
            throw new NovaException("Template ID cannot be null");
        }

        TemplateMetadata metadata = metadataStore.get(templateId);
        if (metadata == null) {
            throw new NovaException("Template metadata not found: " + templateId);
        }

        return metadata;
    }

    private String renderTemplate(String template, Map<String, Object> variables) {
        Matcher matcher = variablePattern.matcher(template);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String variable = matcher.group(1);
            Object value = variables.get(variable);
            if (value == null) {
                throw new NovaException("Required variable not provided: " + variable);
            }
            matcher.appendReplacement(result, Matcher.quoteReplacement(value.toString()));
        }
        matcher.appendTail(result);

        return result.toString();
    }

    private String generateVersion(String templateId) {
        Map<String, String> versions = templateStore.get(templateId);
        if (versions == null) {
            return "1.0.0";
        }
        
        // Find highest version number and increment
        return versions.keySet().stream()
            .map(this::parseVersion)
            .max(Integer::compareTo)
            .map(v -> String.format("%d.0.0", v + 1))
            .orElse("1.0.0");
    }

    private Integer parseVersion(String version) {
        try {
            return Integer.parseInt(version.split("\\.")[0]);
        } catch (Exception e) {
            return 0;
        }
    }

    private String getLatestVersion(Map<String, String> versions) {
        return versions.entrySet().stream()
            .max(Map.Entry.comparingByKey(this::compareVersions))
            .map(Map.Entry::getValue)
            .orElse(null);
    }

    private int compareVersions(String v1, String v2) {
        String[] parts1 = v1.split("\\.");
        String[] parts2 = v2.split("\\.");
        
        for (int i = 0; i < Math.min(parts1.length, parts2.length); i++) {
            int num1 = Integer.parseInt(parts1[i]);
            int num2 = Integer.parseInt(parts2[i]);
            if (num1 != num2) {
                return num1 - num2;
            }
        }
        
        return parts1.length - parts2.length;
    }

    private void updateMetadata(String templateId, String version) {
        TemplateMetadata metadata = metadataStore.computeIfAbsent(
            templateId,
            k -> new TemplateMetadata(templateId, templateId, version)
        );
        
        metadata.setVersion(version);
        metadata.setUpdatedAt(java.time.LocalDateTime.now());
    }
} 