package com.terra.framework.nova.prompt.config;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Configuration for prompt execution.
 * <p>
 * This class holds configuration parameters such as model specification,
 * temperature, max tokens, and other model-specific parameters.
 *
 * @author DeavyJones
 */
public class PromptConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * The model client bean name or class name to use for this prompt.
     */
    private String model;

    /**
     * Temperature parameter for controlling randomness (0.0 to 1.0).
     */
    private Double temperature;

    /**
     * Maximum number of tokens to generate.
     */
    private Integer maxTokens;

    /**
     * Top-p parameter for nucleus sampling.
     */
    private Double topP;

    /**
     * Custom parameters for model-specific configurations.
     */
    private Map<String, Object> customParams;

    public PromptConfig() {
        this.customParams = new HashMap<>();
    }

    public PromptConfig(String model, Double temperature, Integer maxTokens, Double topP) {
        this();
        this.model = model;
        this.temperature = temperature;
        this.maxTokens = maxTokens;
        this.topP = topP;
    }

    /**
     * Merges this config with a parent config.
     * Current config values take precedence over parent values.
     *
     * @param parentConfig The parent configuration to merge with
     * @return A new merged configuration
     */
    public PromptConfig mergeWith(PromptConfig parentConfig) {
        if (parentConfig == null) {
            return this;
        }

        PromptConfig merged = new PromptConfig();
        merged.model = this.model != null ? this.model : parentConfig.model;
        merged.temperature = this.temperature != null ? this.temperature : parentConfig.temperature;
        merged.maxTokens = this.maxTokens != null ? this.maxTokens : parentConfig.maxTokens;
        merged.topP = this.topP != null ? this.topP : parentConfig.topP;

        // Merge custom parameters
        merged.customParams.putAll(parentConfig.customParams);
        merged.customParams.putAll(this.customParams);

        return merged;
    }

    // Getters and setters
    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public Double getTemperature() {
        return temperature;
    }

    public void setTemperature(Double temperature) {
        this.temperature = temperature;
    }

    public Integer getMaxTokens() {
        return maxTokens;
    }

    public void setMaxTokens(Integer maxTokens) {
        this.maxTokens = maxTokens;
    }

    public Double getTopP() {
        return topP;
    }

    public void setTopP(Double topP) {
        this.topP = topP;
    }

    public Map<String, Object> getCustomParams() {
        return customParams;
    }

    public void setCustomParams(Map<String, Object> customParams) {
        this.customParams = customParams != null ? customParams : new HashMap<>();
    }

    public void addCustomParam(String key, Object value) {
        this.customParams.put(key, value);
    }

    @Override
    public String toString() {
        return "PromptConfig{" +
                "model='" + model + '\'' +
                ", temperature=" + temperature +
                ", maxTokens=" + maxTokens +
                ", topP=" + topP +
                ", customParams=" + customParams +
                '}';
    }
} 