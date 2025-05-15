package com.terra.framework.nova.gpt.model;

import java.util.Map;

public class RoutingContext {
    private String requestId;
    private String prompt;
    private Map<String, Object> requirements;
    private Map<String, Object> preferences;
    private Map<String, Object> constraints;

    public RoutingContext(String requestId, String prompt) {
        this.requestId = requestId;
        this.prompt = prompt;
    }

    // Getters and setters
    public String getRequestId() { return requestId; }
    public void setRequestId(String requestId) { this.requestId = requestId; }

    public String getPrompt() { return prompt; }
    public void setPrompt(String prompt) { this.prompt = prompt; }

    public Map<String, Object> getRequirements() { return requirements; }
    public void setRequirements(Map<String, Object> requirements) { this.requirements = requirements; }

    public Map<String, Object> getPreferences() { return preferences; }
    public void setPreferences(Map<String, Object> preferences) { this.preferences = preferences; }

    public Map<String, Object> getConstraints() { return constraints; }
    public void setConstraints(Map<String, Object> constraints) { this.constraints = constraints; }
} 