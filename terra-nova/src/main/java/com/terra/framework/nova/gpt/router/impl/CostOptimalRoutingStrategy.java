package com.terra.framework.nova.gpt.router.impl;

import com.terra.framework.nova.gpt.model.ModelInstance;
import com.terra.framework.nova.gpt.model.RoutingContext;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.Map;
import java.util.Optional;

@Component
public class CostOptimalRoutingStrategy implements RoutingStrategy {
    
    @Override
    public ModelInstance selectModel(RoutingContext context, Map<String, ModelInstance> modelRegistry) {
        return modelRegistry.values().stream()
            .filter(model -> meetsRequirements(model, context))
            .min(Comparator.comparingDouble(model -> calculateCost(model, context)))
            .orElse(getFallbackModel(modelRegistry));
    }

    private boolean meetsRequirements(ModelInstance model, RoutingContext context) {
        Map<String, Object> requirements = context.getRequirements();
        if (requirements == null || requirements.isEmpty()) {
            return true;
        }

        Map<String, Object> capabilities = model.getMetadata().getCapabilities();
        if (capabilities == null) {
            return false;
        }

        return requirements.entrySet().stream()
            .allMatch(entry -> {
                Object required = entry.getValue();
                Object available = capabilities.get(entry.getKey());
                return isCapabilityMet(required, available);
            });
    }

    private boolean isCapabilityMet(Object required, Object available) {
        if (required == null || available == null) {
            return false;
        }

        if (required instanceof Number && available instanceof Number) {
            return ((Number) available).doubleValue() >= ((Number) required).doubleValue();
        }

        if (required instanceof Boolean && available instanceof Boolean) {
            return ((Boolean) available).equals(required);
        }

        return available.toString().equals(required.toString());
    }

    private double calculateCost(ModelInstance model, RoutingContext context) {
        Map<String, Object> costs = model.getMetadata().getCosts();
        if (costs == null) {
            return Double.MAX_VALUE;
        }

        // Basic cost calculation based on token count
        double baseTokenCost = getDoubleValue(costs.get("token_cost"), 0.0);
        int estimatedTokens = estimateTokenCount(context.getPrompt());
        double tokenCost = baseTokenCost * estimatedTokens;

        // Additional cost factors
        double latencyCost = getDoubleValue(costs.get("latency_cost"), 0.0);
        double priorityCost = calculatePriorityCost(context);

        return tokenCost + latencyCost + priorityCost;
    }

    private double getDoubleValue(Object value, double defaultValue) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        return defaultValue;
    }

    private int estimateTokenCount(String prompt) {
        // Simple estimation: ~4 characters per token
        return prompt != null ? prompt.length() / 4 : 0;
    }

    private double calculatePriorityCost(RoutingContext context) {
        Map<String, Object> preferences = context.getPreferences();
        if (preferences == null || !preferences.containsKey("priority")) {
            return 0.0;
        }

        Object priority = preferences.get("priority");
        if (priority instanceof Number) {
            return ((Number) priority).doubleValue();
        }
        return 0.0;
    }

    private ModelInstance getFallbackModel(Map<String, ModelInstance> modelRegistry) {
        // Get the most general-purpose model as fallback
        return modelRegistry.values().stream()
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No fallback model available"));
    }
} 