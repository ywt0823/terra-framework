package com.terra.framework.nova.gpt.model;

import java.util.Map;

public class ModelMetadata {
    private String modelId;
    private String name;
    private String version;
    private String description;
    private Map<String, Object> capabilities;
    private Map<String, Object> constraints;
    private Map<String, Object> performance;
    private Map<String, Object> costs;

    public ModelMetadata(String modelId, String name, String version) {
        this.modelId = modelId;
        this.name = name;
        this.version = version;
    }

    // Getters and setters
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getVersion() { return version; }
    public void setVersion(String version) { this.version = version; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public Map<String, Object> getCapabilities() { return capabilities; }
    public void setCapabilities(Map<String, Object> capabilities) { this.capabilities = capabilities; }

    public Map<String, Object> getConstraints() { return constraints; }
    public void setConstraints(Map<String, Object> constraints) { this.constraints = constraints; }

    public Map<String, Object> getPerformance() { return performance; }
    public void setPerformance(Map<String, Object> performance) { this.performance = performance; }

    public Map<String, Object> getCosts() { return costs; }
    public void setCosts(Map<String, Object> costs) { this.costs = costs; }
} 