package com.terra.framework.nova.tuner.optimizer;

import com.terra.framework.nova.tuner.model.OptimizationConfig;
import com.terra.framework.nova.tuner.model.OptimizationResult;
import com.terra.framework.nova.tuner.model.PerformanceMetrics;

public interface ParameterOptimizer {
    /**
     * Optimize model parameters based on the given configuration
     *
     * @param config The optimization configuration
     * @return The optimization result
     */
    OptimizationResult optimize(OptimizationConfig config);

    /**
     * Evaluate model performance with current parameters
     *
     * @param modelId The ID of the model to evaluate
     * @param metrics The performance metrics to evaluate
     */
    void evaluatePerformance(String modelId, PerformanceMetrics metrics);

    /**
     * Get the current optimization status
     *
     * @param optimizationId The ID of the optimization process
     * @return The current optimization result
     */
    OptimizationResult getStatus(String optimizationId);
} 