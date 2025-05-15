package com.terra.framework.nova.tuner.model;

import java.util.Map;
import java.util.List;

public class OptimizationConfig {
    private String modelId;
    private Map<String, Object> parameters;
    private List<String> objectives;
    private Map<String, Object> constraints;
    private OptimizationStrategy strategy;
    private int maxIterations;
    private double targetScore;

    public OptimizationConfig(String modelId, Map<String, Object> parameters) {
        this.modelId = modelId;
        this.parameters = parameters;
    }

    // Getters and setters
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }

    public Map<String, Object> getParameters() { return parameters; }
    public void setParameters(Map<String, Object> parameters) { this.parameters = parameters; }

    public List<String> getObjectives() { return objectives; }
    public void setObjectives(List<String> objectives) { this.objectives = objectives; }

    public Map<String, Object> getConstraints() { return constraints; }
    public void setConstraints(Map<String, Object> constraints) { this.constraints = constraints; }

    public OptimizationStrategy getStrategy() { return strategy; }
    public void setStrategy(OptimizationStrategy strategy) { this.strategy = strategy; }

    public int getMaxIterations() { return maxIterations; }
    public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }

    public double getTargetScore() { return targetScore; }
    public void setTargetScore(double targetScore) { this.targetScore = targetScore; }
} 