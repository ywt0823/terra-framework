package com.terra.framework.nova.tuner.model;

import java.util.Map;

public class PerformanceMetrics {
    private String modelId;
    private double latency;
    private double throughput;
    private double accuracy;
    private double cost;
    private Map<String, Double> customMetrics;

    public PerformanceMetrics(String modelId) {
        this.modelId = modelId;
    }

    // Getters and setters
    public String getModelId() { return modelId; }
    public void setModelId(String modelId) { this.modelId = modelId; }

    public double getLatency() { return latency; }
    public void setLatency(double latency) { this.latency = latency; }

    public double getThroughput() { return throughput; }
    public void setThroughput(double throughput) { this.throughput = throughput; }

    public double getAccuracy() { return accuracy; }
    public void setAccuracy(double accuracy) { this.accuracy = accuracy; }

    public double getCost() { return cost; }
    public void setCost(double cost) { this.cost = cost; }

    public Map<String, Double> getCustomMetrics() { return customMetrics; }
    public void setCustomMetrics(Map<String, Double> customMetrics) { this.customMetrics = customMetrics; }

    public void addCustomMetric(String name, Double value) {
        this.customMetrics.put(name, value);
    }
} 