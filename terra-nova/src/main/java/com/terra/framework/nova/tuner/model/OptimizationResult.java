package com.terra.framework.nova.tuner.model;

import java.util.Map;
import java.util.List;

public class OptimizationResult {
    private String optimizationId;
    private String modelId;
    private Map<String, Object> bestParameters;
    private double bestScore;
    private List<Map<String, Object>> history;
    private OptimizationStatus status;
    private String message;

    public OptimizationResult(String optimizationId, String modelId) {
        this.optimizationId = optimizationId;
        this.modelId = modelId;
    }

    // Getters and setters
    public String getOptimizationId() { return optimizationId; }
    public void setOptimizationId(String optimizationId) { this.optimizationId = optimizationId; }

    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }

    public Map<String, Object> getBestParameters() { return bestParameters; }
    public void setBestParameters(Map<String, Object> bestParameters) { this.bestParameters = bestParameters; }

    public double getBestScore() { return bestScore; }
    public void setBestScore(double bestScore) { this.bestScore = bestScore; }

    public List<Map<String, Object>> getHistory() { return history; }
    public void setHistory(List<Map<String, Object>> history) { this.history = history; }

    public OptimizationStatus getStatus() { return status; }
    public void setStatus(OptimizationStatus status) { this.status = status; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public enum OptimizationStatus {
        RUNNING,
        COMPLETED,
        FAILED,
        STOPPED
    }
} 