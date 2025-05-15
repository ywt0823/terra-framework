package com.terra.framework.nova.prompt.template;

import java.util.Map;

public interface PromptTemplate {
    /**
     * Render the template with the given variables
     *
     * @param variables The variables to use in template rendering
     * @return The rendered prompt
     */
    String render(Map<String, Object> variables);

    /**
     * Save the template with the given ID
     *
     * @param templateId The ID to save the template under
     * @param content The template content
     */
    void save(String templateId, String content);

    /**
     * Load a template by ID and version
     *
     * @param templateId The ID of the template to load
     * @param version The version of the template to load (optional)
     * @return The template content
     */
    String load(String templateId, String version);

    /**
     * Get the template metadata
     *
     * @param templateId The ID of the template
     * @return The template metadata
     */
    TemplateMetadata getMetadata(String templateId);
} 