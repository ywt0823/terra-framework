# Terra Framework

Terra Framework 是一个现代化的 Java 企业级应用开发框架，专注于大语言模型(LLM)的集成与应用，提供了一套完整的解决方案。

## 核心特性

- **模型集成与管理**：支持多种大语言模型的统一接入与管理
- **Spring AI 深度集成**：基于 Spring AI 1.0+ 构建，提供原生 Spring Boot 体验
- **模型混合与增强**：提供模型混合与结果合并能力
- **企业级增强功能**：智能缓存、重试机制、监控观测、安全防护
- **高度可扩展**：清晰的接口设计，便于自定义实现
- **易于集成**：Spring Boot 友好，支持自动配置
- **多模型供应商支持**：支持 OpenAI、Anthropic Claude、Ollama、百度文心一言、阿里通义千问等多种模型

## Spring AI 集成架构

Terra Framework 基于 Spring AI 1.0+ 构建了四层架构：

### 应用层 (Application Layer)
- **Chat Client**：基于 Spring AI ChatClient 的增强实现
- **RAG Service**：检索增强生成服务，支持向量数据库集成
- **Agent System**：智能代理系统，支持工具调用和复杂任务处理

### 增强层 (Enhancement Layer)
- **智能缓存**：语义缓存和传统缓存结合，提升响应速度
- **监控观测**：基于 Micrometer 的完整可观测性支持
- **安全防护**：API 密钥管理、请求限流、内容过滤

### 集成层 (Integration Layer)
- **Spring AI Bridge**：Terra Framework 到 Spring AI 的桥接器
- **Model Adapter**：多模型提供商的统一适配
- **Service Wrapper**：服务包装器，提供增强功能

### 核心层 (Core Layer)
- **AI Model Abstraction**：AI 模型抽象层
- **Provider Management**：模型提供商管理
- **Configuration Management**：配置管理系统

## 项目结构

Terra Framework 由以下核心模块组成：

| 模块名称 | 描述 | 主要功能 |
|---------|------|----------|
| [terra-dependencies](#terra-dependencies) | 依赖管理模块 | 统一管理所有第三方依赖版本 |
| [terra-bedrock](#terra-bedrock) | 核心基础设施模块 | 异常处理、统一响应、安全框架、事件机制 |
| [terra-nova](#terra-nova) | LLM 集成与应用框架 | 模型管理、模型混合、LLM 服务、Spring AI 集成 |
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
- Spring AI 1.0+

### 添加依赖

使用 Terra Framework 最简单的方式是通过 Spring Boot Starter：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-nova-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 基本配置

在 `application.properties` 或 `application.yml` 中添加：

```yaml
terra:
  nova:
    enabled: true
    # Spring AI 集成配置
    spring-ai:
      enabled: true
      auto-register-models: true
    
    # 模型配置
    models:
      default-provider: openai
      providers:
        openai:
          api-key: ${OPENAI_API_KEY}
          base-url: https://api.openai.com/v1
          models:
            - name: gpt-4o
              type: CHAT
              max-tokens: 4096
              temperature: 0.7
        anthropic:
          api-key: ${ANTHROPIC_API_KEY}
          models:
            - name: claude-3-5-sonnet-20241022
              type: CHAT
              max-tokens: 4096
    
    # 增强功能配置
    enhancement:
      cache:
        enabled: true
        ttl: 3600s
        similarity-threshold: 0.95
      retry:
        enabled: true
        max-attempts: 3
      monitoring:
        enabled: true
    
    # RAG 配置
    rag:
      enabled: true
      vector-store:
        type: redis
        similarity-threshold: 0.8
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

LLM 集成与应用框架，是 Terra Framework 的核心模块，基于 Spring AI 1.0+ 构建。

#### 主要功能

1. **Spring AI 深度集成**
   - 基于 Spring AI 1.0+ 的 ChatClient API
   - 支持 20+ 种 AI 模型提供商
   - 多模态支持（文本、图像、音频）
   - 统一的 API 抽象

2. **模型管理与适配**
   - 支持多种 LLM 模型：OpenAI、Claude、Ollama、文心一言、通义千问等
   - 统一的模型接口与请求格式
   - 灵活的认证机制
   - 动态模型切换

3. **增强型 AI 服务**
   - 智能缓存（语义缓存 + 传统缓存）
   - 重试机制与熔断保护
   - 结果后处理与验证
   - 请求监控与追踪
   - 异常处理与降级

4. **RAG 系统**
   - 基于 Spring AI 的向量存储抽象
   - 支持 20+ 种向量数据库
   - 统一的 SQL-like 查询语法
   - 文档分块与嵌入生成
   - 检索策略优化

5. **Agent系统与工具**
   - 基于 Spring AI 的 Function Calling
   - 多种Agent类型支持（ReAct、Plan-and-Execute）
   - 工具注册与管理系统
   - 基于注解的工具开发
   - 内置常用工具集（计算器、日期时间处理、文本分析等）
   - 记忆管理和上下文维护

6. **可观测性与监控**
   - 基于 Micrometer 的指标收集
   - 分布式追踪支持
   - 模型调用统计
   - 成本分析与优化建议
   - 健康检查与告警

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
// 使用 Terra Chat Client（基于 Spring AI ChatClient）
@Autowired
private TerraChatClient terraChatClient;

// 简单聊天
String response = terraChatClient.chat("你好，请介绍一下自己");

// 结构化输出
record Joke(String setup, String punchline) {}
Joke joke = terraChatClient.prompt()
    .user("Tell me a joke")
    .call()
    .entity(Joke.class);

// 使用 RAG
@Autowired
private TerraRAGService ragService;

String answer = ragService.query("什么是量子计算？", 
    QueryOptions.builder()
        .maxResults(5)
        .similarityThreshold(0.8)
        .build());

// 使用工具调用
@Component
public class WeatherTool {
    @Tool("Get current weather for a city")
    public Weather getCurrentWeather(@ToolParam("city name") String city) {
        // 实现天气查询逻辑
        return new Weather(city, "晴天", 25);
    }
}

// Agent 使用
@Autowired
private TerraAgentService agentService;

AgentResponse response = agentService.execute(
    AgentRequest.builder()
        .instruction("帮我查询北京的天气，然后推荐合适的穿衣建议")
        .tools(List.of("getCurrentWeather", "getClothingAdvice"))
        .build());
```

**配置示例**：
```yaml
terra:
  nova:
    # Spring AI 集成
    spring-ai:
      enabled: true
      auto-register-models: true
    
    # 模型配置
    models:
      default-provider: openai
      providers:
        openai:
          api-key: ${OPENAI_API_KEY}
          base-url: https://api.openai.com/v1
          models:
            - name: gpt-4o
              type: CHAT
              max-tokens: 4096
              temperature: 0.7
            - name: gpt-3.5-turbo
              type: CHAT
              max-tokens: 4096
              temperature: 0.7
            - name: text-embedding-3-small
              type: EMBEDDING
              max-tokens: 8191
        
        anthropic:
          api-key: ${ANTHROPIC_API_KEY}
          models:
            - name: claude-3-5-sonnet-20241022
              type: CHAT
              max-tokens: 4096
              temperature: 0.7
            - name: claude-3-haiku-20240307
              type: CHAT
              max-tokens: 4096
              temperature: 0.7
        
        ollama:
          base-url: http://localhost:11434
          models:
            - name: llama2
              type: CHAT
              max-tokens: 4096
              temperature: 0.7
    
    # 增强功能
    enhancement:
      # 智能缓存
      cache:
        enabled: true
        ttl: 3600s  # 1小时
        similarity-threshold: 0.95  # 语义相似度阈值
        max-size: 1000
      
      # 重试机制
      retry:
        enabled: true
        max-attempts: 3
        backoff:
          initial-interval: 1000ms
          multiplier: 2.0
          max-interval: 10000ms
      
      # 监控配置
      monitoring:
        enabled: true
        metrics:
          enabled: true
          export:
            prometheus:
              enabled: true
        tracing:
          enabled: true
          sampling-probability: 0.1
    
    # RAG 配置
    rag:
      enabled: true
      vector-store:
        type: redis
        similarity-threshold: 0.8
        max-results: 10
      document:
        chunk-size: 1000
        chunk-overlap: 200
        metadata-extraction: true
    
    # Agent 配置
    agent:
      enabled: true
      default-type: react
      max-iterations: 10
      timeout: 30s
      tools:
        auto-discovery: true
        packages: ["com.terra.framework.nova.tools"]
    
    # 向量存储配置
    vector-stores:
      redis:
        host: localhost
        port: 6379
        index-name: terra-vectors
        dimension: 1536
      postgresql:
        url: jdbc:postgresql://localhost:5432/terra
        username: ${DB_USERNAME}
        password: ${DB_PASSWORD}
        table-name: vector_store
        dimension: 1536
    
    # 可观测性
    observability:
      metrics:
        enabled: true
        tags:
          application: terra-nova
          environment: ${ENVIRONMENT:dev}
      tracing:
        enabled: true
        service-name: terra-nova
        sampling-rate: 0.1
      logging:
        level: INFO
        include-request-details: true
        include-response-details: false  # 避免记录敏感信息
```

### Spring AI 集成特性

#### 1. 无缝集成
- 基于 Spring AI 1.0+ 构建，提供原生 Spring Boot 体验
- 自动配置和依赖注入
- 与 Spring 生态系统完美集成

#### 2. 多模型支持
- 统一接口支持 20+ 种 AI 模型提供商
- 动态模型切换和负载均衡
- 模型特定功能的访问

#### 3. 企业级增强
- 智能缓存：语义缓存 + 传统缓存
- 重试机制：指数退避和熔断保护
- 监控观测：完整的 Micrometer 集成
- 安全防护：API 密钥管理和内容过滤

#### 4. RAG 系统
- 基于 Spring AI 的向量存储抽象
- 支持多种向量数据库
- 智能文档处理和检索优化

#### 5. Agent 系统
- 基于 Spring AI 的 Function Calling
- 声明式工具定义
- 复杂任务的自动分解和执行

#### 6. 可观测性
- 模型调用指标和追踪
- 成本分析和优化建议
- 健康检查和告警机制

### 技术优势

1. **配置驱动**：通过配置文件即可切换模型提供商，无需代码修改
2. **高可用性**：内置重试、熔断、降级机制
3. **性能优化**：智能缓存和连接池管理
4. **安全可靠**：API 密钥安全管理和内容过滤
5. **可扩展性**：清晰的接口设计，便于自定义实现
6. **生产就绪**：完整的监控、日志和健康检查

### 使用场景

1. **智能客服**：基于 RAG 的知识库问答
2. **内容生成**：文档、报告、代码生成
3. **数据分析**：自然语言查询和分析
4. **工作流自动化**：基于 Agent 的任务自动化
5. **多模态应用**：文本、图像、音频的综合处理

### 最佳实践

1. **模型选择**：根据任务类型选择合适的模型
2. **缓存策略**：合理配置缓存 TTL 和相似度阈值
3. **监控告警**：设置合适的监控指标和告警规则
4. **成本控制**：监控 Token 使用量和 API 调用成本
5. **安全防护**：定期轮换 API 密钥，设置内容过滤规则

## 技术栈

- **Java 17+**：现代 Java 特性支持
- **Spring Boot 3.x**：企业级应用框架
- **Spring AI 1.0+**：AI 应用开发框架
- **Maven 3.8.x**：项目构建和依赖管理
- **Micrometer**：可观测性和监控
- **Redis/PostgreSQL**：向量存储和缓存
- **Docker**：容器化部署

## 贡献指南

我们欢迎社区贡献！请查看 [CONTRIBUTING.md](CONTRIBUTING.md) 了解如何参与项目开发。

## 许可证

本项目采用 [Apache License 2.0](LICENSE) 许可证。

## 联系我们

- 项目主页：https://github.com/terra-framework/terra-framework
- 问题反馈：https://github.com/terra-framework/terra-framework/issues
- 讨论区：https://github.com/terra-framework/terra-framework/discussions

---

**Terra Framework - 让 AI 应用开发更简单、更可靠、更高效！** 