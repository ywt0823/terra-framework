package com.terra.framework.nova.gpt.model;

import java.util.Map;

public class ModelInstance {
    private String modelId;
    private String provider;
    private String endpoint;
    private Map<String, Object> parameters;
    private ModelMetadata metadata;

    public ModelInstance(String modelId, String provider, String endpoint, Map<String, Object> parameters, ModelMetadata metadata) {
        this.modelId = modelId;
        this.provider = provider;
        this.endpoint = endpoint;
        this.parameters = parameters;
        this.metadata = metadata;
    }

    // Getters and setters
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }
    
    public String getProvider() { return provider; }
    public void setProvider(String provider) { this.provider = provider; }
    
    public String getEndpoint() { return endpoint; }
    public void setEndpoint(String endpoint) { this.endpoint = endpoint; }
    
    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }
    
    public ModelMetadata getMetadata() { return metadata; }
    public void setMetadata(ModelMetadata metadata) { this.metadata = metadata; }
} 