package com.terra.framework.nova.gpt.router.impl;

import com.terra.framework.nova.gpt.model.ModelInstance;
import com.terra.framework.nova.gpt.model.ModelMetadata;
import com.terra.framework.nova.gpt.model.RoutingContext;
import com.terra.framework.nova.gpt.router.ModelRouter;
import com.terra.framework.nova.core.exception.NovaException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Comparator;
import java.util.Optional;

@Service
public class DefaultModelRouter implements ModelRouter {
    private final Map<String, ModelInstance> modelRegistry = new ConcurrentHashMap<>();
    private final RoutingStrategy routingStrategy;

    public DefaultModelRouter(RoutingStrategy routingStrategy) {
        this.routingStrategy = routingStrategy;
    }

    @Override
    public ModelInstance route(RoutingContext context) {
        if (modelRegistry.isEmpty()) {
            throw new NovaException("No models registered in the router");
        }

        return routingStrategy.selectModel(context, modelRegistry);
    }

    @Override
    public void registerModel(ModelMetadata metadata) {
        if (metadata == null || metadata.getModelId() == null) {
            throw new NovaException("Invalid model metadata");
        }

        ModelInstance instance = createModelInstance(metadata);
        modelRegistry.put(metadata.getModelId(), instance);
    }

    @Override
    public void unregisterModel(String modelId) {
        if (modelId == null) {
            throw new NovaException("Model ID cannot be null");
        }

        if (!modelRegistry.containsKey(modelId)) {
            throw new NovaException("Model not found: " + modelId);
        }

        modelRegistry.remove(modelId);
    }

    private ModelInstance createModelInstance(ModelMetadata metadata) {
        // Default parameters for the model instance
        Map<String, Object> defaultParams = Map.of(
            "temperature", 0.7,
            "max_tokens", 2048,
            "top_p", 1.0
        );

        return new ModelInstance(
            metadata.getModelId(),
            determineProvider(metadata),
            determineEndpoint(metadata),
            defaultParams,
            metadata
        );
    }

    private String determineProvider(ModelMetadata metadata) {
        // Logic to determine the provider based on model capabilities and constraints
        Map<String, Object> capabilities = metadata.getCapabilities();
        if (capabilities != null && capabilities.containsKey("provider")) {
            return capabilities.get("provider").toString();
        }
        return "openai"; // Default provider
    }

    private String determineEndpoint(ModelMetadata metadata) {
        // Logic to determine the endpoint based on model metadata
        Map<String, Object> capabilities = metadata.getCapabilities();
        if (capabilities != null && capabilities.containsKey("endpoint")) {
            return capabilities.get("endpoint").toString();
        }
        return "https://api.openai.com/v1/chat/completions"; // Default endpoint
    }
} 