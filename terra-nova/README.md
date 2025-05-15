# Terra Nova

Terra Nova is a powerful LLM (Large Language Model) pipeline and prompt engineering framework built on top of Spring AI and LangChain4j. It provides a comprehensive set of tools for model routing, parameter optimization, and prompt management.

## Features

### NovaGPT
- Multi-model routing system
- Cost optimization
- Performance optimization
- Model registry and health monitoring
- Load balancing
- Multiple model provider support (OpenAI, Azure OpenAI, Anthropic, etc.)

### StellarTuner
- Automated parameter optimization
- A/B testing framework
- Performance analysis
- Multiple optimization strategies:
  - Bayesian optimization
  - Evolutionary algorithms
  - Gradient descent
  - Random search
  - Grid search

### Prompt Engine
- Template management
- Version control
- Variable interpolation
- Multi-language support
- Collaborative editing

## Getting Started

### Prerequisites
- Java 17 or higher
- Spring Boot 3.x
- Maven

### Installation

Add the following dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-nova</artifactId>
    <version>${terra.version}</version>
</dependency>
```

### Basic Usage

1. Model Routing:
```java
@Autowired
private ModelRouter modelRouter;

public String processPrompt(String prompt) {
    RoutingContext context = new RoutingContext("request-123", prompt);
    ModelInstance model = modelRouter.route(context);
    // Use the model instance
}
```

2. Parameter Optimization:
```java
@Autowired
private ParameterOptimizer optimizer;

public void optimizeModel(String modelId) {
    OptimizationConfig config = new OptimizationConfig(modelId, defaultParameters);
    config.setStrategy(OptimizationStrategy.BAYESIAN_OPTIMIZATION);
    OptimizationResult result = optimizer.optimize(config);
    // Use the optimized parameters
}
```

3. Prompt Templates:
```java
@Autowired
private PromptTemplate promptTemplate;

public String generatePrompt(Map<String, Object> variables) {
    String renderedPrompt = promptTemplate.render(variables);
    // Use the rendered prompt
}
```

## Configuration

Example configuration in `application.yml`:

```yaml
terra:
  nova:
    gpt:
      default-provider: openai
      routing:
        strategy: cost-optimal
        fallback-enabled: true
    tuner:
      optimization:
        default-strategy: bayesian
        max-iterations: 100
    prompt:
      template:
        storage: file-system
        version-control: enabled
```

## Contributing

Please read [CONTRIBUTING.md](../CONTRIBUTING.md) for details on our code of conduct and the process for submitting pull requests.

## License

This project is licensed under the same license as the parent Terra Framework project - see the [LICENSE](../LICENSE) file for details. 