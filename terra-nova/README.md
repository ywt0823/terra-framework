# Terra Nova

Terra Nova 是 Terra Framework 的核心子项目，专注于大语言模型(LLM)的统一接入与增强。它提供了一套完整的工具集，用于模型管理、多模型混合调用和 AI 服务增强。

## 核心功能

### 1. 模型管理与适配

Terra Nova 支持多种大语言模型的统一接入，目前已实现的模型适配器包括：

- **OpenAI 系列**：支持 GPT-3.5、GPT-4 等模型
- **Anthropic Claude**：支持 Claude 3 系列模型
- **文心一言**：百度大语言模型
- **通义千问**：阿里巴巴大语言模型
- **DeepSeek**：DeepSeek AI 大语言模型
- **Ollama**：本地化部署模型支持
- **Dify**：支持 Dify 平台集成

主要特性：
- 统一的模型接口与请求格式
- 灵活的认证机制（API Key、Bearer Token 等）
- 模型状态监控与自动重试
- 模型信息注册与管理

### 2. 模型混合系统（Model Blender）

Terra Nova 提供强大的模型混合能力，可以同时调用多个 LLM 模型并智能合并结果：

- **多策略支持**：
  - 加权平均：根据配置的权重对多个模型响应进行合并
  - 最佳质量：选择质量最高的响应
  - 最快响应：选择响应最快的结果
  - 投票决策：多模型结果投票决定最终输出

- **结果合并机制**：
  - 智能文本合并
  - JSON 结构化数据合并
  - 自定义合并策略扩展

### 3. 增强型 AI 服务

Terra Nova 在基本 AI 服务的基础上提供了多种增强功能：

- **请求重试**：
  - 自动重试失败请求
  - 可配置的退避策略
  - 错误分类与选择性重试

- **结果缓存**：
  - 高效缓存相同请求的响应
  - 可配置的缓存生命周期
  - 多级缓存支持

- **请求监控**：
  - 响应时间监控
  - 模型调用统计
  - 错误率追踪

## 快速开始

### 环境要求

- Java 17 或更高版本
- Spring Boot 3.x
- Maven

### 添加依赖

在你的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-nova</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 基本配置

在 `application.properties` 或 `application.yml` 中添加配置：

```yaml
terra:
  nova:
    model:
      default-provider: openai
      providers:
        openai:
          api-key: ${OPENAI_API_KEY}
          base-url: https://api.openai.com/v1
          models:
            - name: gpt-3.5-turbo
              type: CHAT
              max-tokens: 4096
            - name: gpt-4
              type: CHAT
              max-tokens: 8192
        claude:
          api-key: ${ANTHROPIC_API_KEY}
          models:
            - name: claude-3-haiku
              type: CHAT
              max-tokens: 4096
        ollama:
          base-url: http://localhost:11434
          models:
            - name: llama2
              type: CHAT
    blend:
      enabled: true
      default-strategy: WEIGHTED_AVERAGE
      timeout: 30000
    retry:
      enabled: true
      max-attempts: 3
      backoff:
        initial-interval: 1000
        multiplier: 2.0
        max-interval: 10000
    cache:
      enabled: true
      ttl: 3600
    monitoring:
      enabled: true
```

## 使用示例

### 基础 AI 服务

```java
@Autowired
private AIService aiService;

// 简单聊天请求
String response = aiService.chat("你好，请介绍一下自己");

// 使用特定模型
String response = aiService.chat("请分析这段代码", "gpt-4");

// 完整请求配置
ModelResponse response = aiService.generate(
    ModelRequest.builder()
        .messages(List.of(new Message(MessageRole.USER, "请解析这个JSON数据")))
        .modelName("gpt-3.5-turbo")
        .temperature(0.7)
        .maxTokens(500)
        .build()
);
```

### 增强型 AI 服务

```java
@Autowired
private EnhancedAIService enhancedService;

// 带重试和缓存的请求
ModelResponse response = enhancedService.generateWithRetryAndCache(
    ModelRequest.builder()
        .messages(List.of(new Message(MessageRole.USER, "分析以下数据并提取关键信息")))
        .modelName("gpt-3.5-turbo")
        .build()
);

// 流式响应处理
enhancedService.streamWithRetry(
    ModelRequest.builder()
        .messages(List.of(new Message(MessageRole.USER, "写一篇关于人工智能的文章")))
        .modelName("gpt-4")
        .build(),
    chunk -> {
        // 处理流式响应片段
        System.out.println(chunk.getContent());
    }
);
```

### 模型混合调用

```java
@Autowired
private BlenderService blenderService;

// 简单混合调用
String blendedResponse = blenderService.blend(
    "请解释量子计算的基本原理",
    List.of("gpt-3.5-turbo", "claude-3-haiku"),
    MergeStrategy.BEST_QUALITY
);

// 高级混合调用
BlenderRequest request = BlenderRequest.builder()
    .prompt("分析以下金融数据并提供投资建议")
    .models(List.of("gpt-4", "claude-3-sonnet", "wenxin"))
    .modelWeights(Map.of(
        "gpt-4", 0.5,
        "claude-3-sonnet", 0.3,
        "wenxin", 0.2
    ))
    .mergeStrategy(MergeStrategy.WEIGHTED_AVERAGE)
    .data("附加数据可以在这里传递")
    .timeout(60000)
    .build();

BlenderResponse blendedResponse = blenderService.blend(request);
System.out.println("混合结果: " + blendedResponse.getContent());
System.out.println("参与模型: " + blendedResponse.getParticipatingModels());
System.out.println("执行时间: " + blendedResponse.getExecutionTimeMs() + "ms");
```

### 模型装饰器

Terra Nova 提供了模型装饰器功能，可以对模型请求进行增强：

```java
@Autowired
private AIModelManager modelManager;

// 获取带装饰器的模型
AIModel model = modelManager.getModel("gpt-3.5-turbo");

// 使用默认装饰器
ModelDecoratorOptions options = ModelDecoratorOptions.builder()
    .withRetry(true)
    .withCache(true)
    .withMonitoring(true)
    .build();

AIModel decoratedModel = ModelDecorators.decorate(model, options);

// 使用装饰后的模型
ModelResponse response = decoratedModel.generate(request);
```

## 模型注册与管理

Terra Nova 允许动态注册和管理模型：

```java
@Autowired
private AIModelManager modelManager;

// 注册新模型
ModelInfo modelInfo = ModelInfo.builder()
    .name("custom-model")
    .provider("custom")
    .type(ModelType.CHAT)
    .maxTokens(8192)
    .build();

AIModel customModel = new CustomModelAdapter(modelInfo);
modelManager.registerModel(customModel);

// 查询可用模型
List<ModelInfo> availableModels = modelManager.getAvailableModels();

// 检查模型状态
ModelStatus status = modelManager.checkModelStatus("gpt-4");
```

## 高级配置

### 自定义 Auth Provider

```java
public class CustomAuthProvider implements AuthProvider {
    @Override
    public AuthCredentials getCredentials(String provider) {
        // 实现自定义的认证凭据获取逻辑
        return new AuthCredentials(AuthType.API_KEY, "custom-api-key");
    }
}

// 注册自定义 Auth Provider
@Configuration
public class AIConfig {
    @Bean
    public AuthProvider authProvider() {
        return new CustomAuthProvider();
    }
}
```

### 自定义模型适配器

```java
public class CustomModelAdapter extends AbstractModelAdapter {
    public CustomModelAdapter(ModelInfo modelInfo) {
        super(modelInfo);
    }
    
    @Override
    public ModelResponse generate(ModelRequest request) {
        // 实现自定义的模型调用逻辑
        return ModelResponse.builder()
            .content("自定义模型响应")
            .model(getModelInfo().getName())
            .tokenUsage(new TokenUsage(10, 20, 30))
            .build();
    }
}
```

## 贡献指南

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。

## 许可证

本项目采用与父项目 Terra Framework 相同的许可证 - 详情请参阅 [LICENSE](../LICENSE) 文件。 