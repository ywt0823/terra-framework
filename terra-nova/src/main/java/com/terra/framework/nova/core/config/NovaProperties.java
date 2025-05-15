package com.terra.framework.nova.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

@Configuration
@ConfigurationProperties(prefix = "terra.nova")
public class NovaProperties {
    private Gpt gpt = new Gpt();
    private Tuner tuner = new Tuner();
    private Prompt prompt = new Prompt();

    public static class Gpt {
        private String defaultProvider = "openai";
        private Routing routing = new Routing();
        private Map<String, Object> providers = new HashMap<>();

        public static class Routing {
            private String strategy = "cost-optimal";
            private boolean fallbackEnabled = true;
            private Map<String, Object> strategies = new HashMap<>();

            // Getters and setters
            public String getStrategy() { return strategy; }
            public void setStrategy(String strategy) { this.strategy = strategy; }
            
            public boolean isFallbackEnabled() { return fallbackEnabled; }
            public void setFallbackEnabled(boolean fallbackEnabled) { this.fallbackEnabled = fallbackEnabled; }
            
            public Map<String, Object> getStrategies() { return strategies; }
            public void setStrategies(Map<String, Object> strategies) { this.strategies = strategies; }
        }

        // Getters and setters
        public String getDefaultProvider() { return defaultProvider; }
        public void setDefaultProvider(String defaultProvider) { this.defaultProvider = defaultProvider; }
        
        public Routing getRouting() { return routing; }
        public void setRouting(Routing routing) { this.routing = routing; }
        
        public Map<String, Object> getProviders() { return providers; }
        public void setProviders(Map<String, Object> providers) { this.providers = providers; }
    }

    public static class Tuner {
        private Optimization optimization = new Optimization();

        public static class Optimization {
            private String defaultStrategy = "bayesian";
            private int maxIterations = 100;
            private Map<String, Object> strategies = new HashMap<>();

            // Getters and setters
            public String getDefaultStrategy() { return defaultStrategy; }
            public void setDefaultStrategy(String defaultStrategy) { this.defaultStrategy = defaultStrategy; }
            
            public int getMaxIterations() { return maxIterations; }
            public void setMaxIterations(int maxIterations) { this.maxIterations = maxIterations; }
            
            public Map<String, Object> getStrategies() { return strategies; }
            public void setStrategies(Map<String, Object> strategies) { this.strategies = strategies; }
        }

        // Getters and setters
        public Optimization getOptimization() { return optimization; }
        public void setOptimization(Optimization optimization) { this.optimization = optimization; }
    }

    public static class Prompt {
        private Template template = new Template();

        public static class Template {
            private String storage = "file-system";
            private boolean versionControl = true;
            private Map<String, Object> options = new HashMap<>();

            // Getters and setters
            public String getStorage() { return storage; }
            public void setStorage(String storage) { this.storage = storage; }
            
            public boolean isVersionControl() { return versionControl; }
            public void setVersionControl(boolean versionControl) { this.versionControl = versionControl; }
            
            public Map<String, Object> getOptions() { return options; }
            public void setOptions(Map<String, Object> options) { this.options = options; }
        }

        // Getters and setters
        public Template getTemplate() { return template; }
        public void setTemplate(Template template) { this.template = template; }
    }

    // Getters and setters
    public Gpt getGpt() { return gpt; }
    public void setGpt(Gpt gpt) { this.gpt = gpt; }
    
    public Tuner getTuner() { return tuner; }
    public void setTuner(Tuner tuner) { this.tuner = tuner; }
    
    public Prompt getPrompt() { return prompt; }
    public void setPrompt(Prompt prompt) { this.prompt = prompt; }
} 