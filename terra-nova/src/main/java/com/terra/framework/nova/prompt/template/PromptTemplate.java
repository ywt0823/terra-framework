package com.terra.framework.nova.prompt.template;

import com.terra.framework.nova.prompt.config.PromptConfig;

import java.io.Serializable;
import java.util.Objects;

/**
 * Represents a parsed prompt template from an XML file.
 * <p>
 * This class holds the unique identifier (mapper method name), the actual
 * template content, and configuration parameters for a single prompt.
 *
 * @author DeavyJones
 */
public class PromptTemplate implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The unique ID of the prompt, corresponding to the mapper interface method name.
     */
    private final String id;

    /**
     * The raw prompt template content with placeholders.
     */
    private final String template;

    /**
     * Configuration parameters for this prompt.
     */
    private final PromptConfig config;

    public PromptTemplate(String id, String template) {
        this(id, template, new PromptConfig());
    }

    public PromptTemplate(String id, String template, PromptConfig config) {
        this.id = id;
        this.template = template;
        this.config = config != null ? config : new PromptConfig();
    }

    public String getId() {
        return id;
    }

    public String getTemplate() {
        return template;
    }

    public PromptConfig getConfig() {
        return config;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PromptTemplate that = (PromptTemplate) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "PromptTemplate{" +
                "id='" + id + '\'' +
                ", template='" + template + '\'' +
                ", config=" + config +
                '}';
    }
} 