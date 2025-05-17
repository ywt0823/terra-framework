# Terra Framework

Terra Framework 是一个现代化的 Java 企业级应用开发框架，专注于大语言模型(LLM)的集成与应用，提供了一套完整的解决方案。

## 核心特性

- **模型集成与管理**：支持多种大语言模型的统一接入与管理
- **模型混合与增强**：提供模型混合与结果合并能力
- **高度可扩展**：清晰的接口设计，便于自定义实现
- **易于集成**：Spring Boot 友好，支持自动配置
- **多模型供应商支持**：支持 OpenAI、Anthropic Claude、Ollama、百度文心一言、阿里通义千问等多种模型

## 项目结构

Terra Framework 由以下核心模块组成：

| 模块名称 | 描述 | 主要功能 |
|---------|------|----------|
| [terra-dependencies](#terra-dependencies) | 依赖管理模块 | 统一管理所有第三方依赖版本 |
| [terra-bedrock](#terra-bedrock) | 核心基础设施模块 | 异常处理、统一响应、安全框架、事件机制 |
| [terra-nova](#terra-nova) | LLM 集成与应用框架 | 模型管理、模型混合、LLM 服务 |
| [terra-crust](#terra-crust) | 业务核心模块 | 领域模型、业务规则、状态机 |
| [terra-strata](#terra-strata) | 数据访问层模块 | ORM支持、事务管理、查询增强 |
| [terra-geyser](#terra-geyser) | 缓存处理模块 | 多级缓存、缓存同步、过期策略 |
| [terra-stream](#terra-stream) | 流处理模块 | 消息队列、事件驱动、流式处理 |
| [terra-sediment](#terra-sediment) | 公共工具模块 | 通用工具类、助手函数 |
| [terra-spring-boot-starter](#terra-spring-boot-starter) | Spring Boot 启动器模块 | 自动配置、便捷集成 |

## 快速开始

### 环境要求

- JDK 17 或更高版本
- Maven 3.8.x 或更高版本
- Spring Boot 3.x

### 添加依赖

使用 Terra Framework 最简单的方式是通过 Spring Boot Starter：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 基本配置

在 `application.properties` 或 `application.yml` 中添加：

```yaml
terra:
  enabled: true
  nova:
    enabled: true
    model:
      default-provider: openai
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

## 模块详情

### terra-dependencies

依赖管理模块，负责统一管理框架使用的第三方库版本，避免版本冲突。

**引入方式**：
```xml
<parent>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-dependencies</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</parent>
```

### terra-bedrock

核心基础设施模块，提供框架的基石功能：统一异常处理、响应格式、安全框架、事件机制等。

### terra-nova

LLM 集成与应用框架，是 Terra Framework 的核心模块。

#### 主要功能

1. **模型管理与适配**
   - 支持多种 LLM 模型：OpenAI、Claude、Ollama、文心一言、通义千问等
   - 统一的模型接口与请求格式
   - 灵活的认证机制

2. **模型混合系统 (Model Blender)**
   - 多模型混合调用策略
   - 结果合并与后处理
   - 灵活的合并策略配置

3. **增强型 AI 服务**
   - 重试机制
   - 结果缓存
   - 请求监控
   - 异常处理

**引入方式**：
```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-nova</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**示例**：
```java
// 使用基础 AI 服务
@Autowired
private AIService aiService;

String response = aiService.chat("你好，请介绍一下自己");

// 使用增强型 AI 服务
@Autowired
private EnhancedAIService enhancedAIService;

ModelResponse response = enhancedAIService.generateWithRetryAndCache(
    ModelRequest.builder()
        .messages(List.of(new Message(MessageRole.USER, "分析以下数据并提取关键信息")))
        .modelName("gpt-3.5-turbo")
        .build()
);

// 使用模型混合服务
@Autowired
private BlenderService blenderService;

String blendedResponse = blenderService.blend(
    "请解释量子计算的基本原理",
    List.of("gpt-3.5-turbo", "claude-3-haiku"),
    MergeStrategy.BEST_QUALITY
);
```

**配置示例**：
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
    cache:
      enabled: true
      ttl: 3600
    monitoring:
      enabled: true
```

### terra-crust

业务核心模块，专注于领域模型定义、业务规则和状态机等企业级应用核心功能。

### terra-strata

数据访问层模块，提供ORM支持、事务管理、动态查询等数据访问功能。

### terra-geyser

缓存处理模块，提供多级缓存、缓存同步等高性能缓存解决方案。

### terra-stream

流处理模块，提供消息队列集成、事件驱动架构和流式数据处理功能。

### terra-sediment

公共工具模块，提供各种通用工具类和助手函数，简化开发过程中的常见任务。

### terra-spring-boot-starter

Spring Boot 启动器模块，实现自动配置，简化框架的集成和使用。

## 实际应用场景

1. **智能客服系统**
   - 利用 terra-nova 提供的模型混合能力，同时调用多个 LLM 模型处理用户问题
   - 通过 terra-geyser 缓存常见问题回答，提高响应速度
   - 使用 terra-stream 处理高并发的用户请求

2. **内容生成平台**
   - 使用 terra-nova 的多模型支持，根据不同内容类型选择最合适的 LLM
   - 通过 terra-crust 管理内容生成的业务规则和工作流
   - 利用 terra-strata 存储和检索生成的内容

3. **数据分析与报告生成**
   - 集成 terra-nova 处理和分析结构化数据
   - 利用模型混合功能综合多个模型的分析结果
   - 通过 terra-bedrock 的统一响应格式输出标准化报告

## 版本说明

当前版本：0.0.1-SNAPSHOT

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交变更 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

请参阅 [CONTRIBUTING.md](CONTRIBUTING.md) 了解更多贡献细节。

## 许可证

[Apache License 2.0](LICENSE) 