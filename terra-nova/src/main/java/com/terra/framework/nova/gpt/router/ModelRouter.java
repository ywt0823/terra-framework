package com.terra.framework.nova.gpt.router;

import com.terra.framework.nova.gpt.model.ModelInstance;
import com.terra.framework.nova.gpt.model.ModelMetadata;
import com.terra.framework.nova.gpt.model.RoutingContext;

public interface ModelRouter {
    /**
     * Route the request to the most appropriate model based on the context
     *
     * @param context The routing context containing request details
     * @return The selected model instance
     */
    ModelInstance route(RoutingContext context);

    /**
     * Register a new model with the router
     *
     * @param metadata The model metadata
     */
    void registerModel(ModelMetadata metadata);

    /**
     * Unregister a model from the router
     *
     * @param modelId The ID of the model to unregister
     */
    void unregisterModel(String modelId);
} 