package com.terra.framework.nova.gpt.router.impl;

import com.terra.framework.nova.gpt.model.ModelInstance;
import com.terra.framework.nova.gpt.model.RoutingContext;

import java.util.Map;

public interface RoutingStrategy {
    /**
     * Select the most appropriate model based on the routing context
     *
     * @param context The routing context containing request details
     * @param modelRegistry The registry of available models
     * @return The selected model instance
     */
    ModelInstance selectModel(RoutingContext context, Map<String, ModelInstance> modelRegistry);
} 