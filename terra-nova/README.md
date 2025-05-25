# Terra-Nova: LLM Pipeline and Prompt Engineering Framework

Terra-Nova 是 Terra Framework 的核心 AI 模块，专注于大语言模型(LLM)的集成与应用，提供了一套完整的 Spring AI 集成解决方案。

## 🚀 核心特性

### Spring AI 深度集成
- **无缝集成**: 基于 Spring AI 1.1.0 构建，提供原生 Spring Boot 体验
- **多模型支持**: 统一接口支持 OpenAI、Anthropic、Ollama、百度文心一言、阿里通义千问等
- **智能适配**: 自动处理不同模型的特性差异和响应格式
- **配置驱动**: 通过配置文件即可切换模型提供商，无需代码修改
- **新特性支持**: 支持 Spring AI 1.1.0 的 ChatClient、Function Calling 等新功能

### 企业级增强功能
- **模型混合系统**: 支持多模型并行调用和结果合并策略
- **智能缓存**: 基于语义相似度的智能缓存机制
- **重试机制**: 指数退避重试策略，提高服务可靠性
- **监控观测**: 完整的 Micrometer 指标和分布式追踪支持
- **安全防护**: 内容过滤、速率限制和访问控制

### RAG 和向量数据库
- **向量存储**: 支持多种向量数据库（Redis、PostgreSQL、Chroma等）
- **文档处理**: 智能文档分割、向量化和检索
- **元数据过滤**: 高性能的元数据过滤查询
- **混合检索**: 结合关键词和语义检索的混合策略

### Function Calling 和 Agent 系统
- **工具注册**: 基于注解的工具自动发现和注册
- **Agent 模式**: 支持 ReAct、Plan-and-Execute 等多种 Agent 模式
- **记忆管理**: 对话上下文和长期记忆管理
- **工具链**: 内置常用工具集（计算器、日期时间、文本分析等）

## 📋 系统要求

- **JDK**: 17 或更高版本
- **Spring Boot**: 3.2.x 或更高版本
- **Spring AI**: 1.1.0 或更高版本
- **Maven**: 3.8.x 或更高版本

## 🛠️ 快速开始

### 1. 添加依赖

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-nova-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 2. 基础配置

```yaml
terra:
  nova:
    enabled: true
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
        ttl: 3600
        similarity-threshold: 0.95
      retry:
        enabled: true
        max-attempts: 3
        backoff:
          initial-interval: 1000
          multiplier: 2.0
          max-interval: 10000
      monitoring:
        enabled: true
        metrics-enabled: true
        tracing-enabled: true
    
    # RAG 配置
    rag:
      enabled: true
      vector-store:
        type: redis
        similarity-threshold: 0.8
        top-k: 5
      document:
        chunk-size: 1000
        chunk-overlap: 200
```

### 3. 基本使用

```java
@RestController
public class AIController {
    
    @Autowired
    private TerraChatClient chatClient;
    
    @Autowired
    private TerraRAGService ragService;
    
    // 基础聊天
    @PostMapping("/chat")
    public String chat(@RequestBody String message) {
        return chatClient.prompt(message)
                .call()
                .content();
    }
    
    // 结构化输出
    @PostMapping("/analyze")
    public AnalysisResult analyze(@RequestBody String text) {
        return chatClient.prompt("分析以下文本的情感和主题: " + text)
                .call()
                .entity(AnalysisResult.class);
    }
    
    // RAG 查询
    @PostMapping("/rag-query")
    public String ragQuery(@RequestBody String question) {
        return ragService.query(question);
    }
}
```

## 🏗️ 架构设计

### 核心架构图

```
┌─────────────────────────────────────────────────────────────┐
│                    Terra-Nova Architecture                  │
├─────────────────────────────────────────────────────────────┤
│  Application Layer                                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │ Chat Client │ │ RAG Service │ │ Agent System│           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Enhancement Layer                                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │   Caching   │ │  Monitoring │ │  Security   │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Integration Layer                                          │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │Spring AI    │ │Model Adapter│ │Service      │           │
│  │Bridge       │ │             │ │Wrapper      │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Core Layer                                                 │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │AI Model     │ │Provider     │ │Configuration│           │
│  │Abstraction  │ │Management   │ │Management   │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
├─────────────────────────────────────────────────────────────┤
│  Spring AI Foundation                                       │
│  ┌─────────────┐ ┌─────────────┐ ┌─────────────┐           │
│  │Chat Models  │ │Vector Stores│ │Function     │           │
│  │             │ │             │ │Calling      │           │
│  └─────────────┘ └─────────────┘ └─────────────┘           │
└─────────────────────────────────────────────────────────────┘
```

### 关键组件说明

#### 1. TerraChatClient
基于 Spring AI ChatClient 的增强版本，提供：
- 统一的聊天接口
- 自动重试和缓存
- 结构化输出支持
- 流式响应处理

#### 2. TerraModelManager
模型管理器，负责：
- 模型注册和发现
- 负载均衡和故障转移
- 模型性能监控
- 动态配置更新

#### 3. TerraRAGService
检索增强生成服务，包含：
- 文档向量化和存储
- 智能检索和排序
- 上下文构建和优化
- 答案生成和后处理

#### 4. TerraAgentSystem
智能代理系统，支持：
- 多种 Agent 模式
- 工具注册和调用
- 任务规划和执行
- 记忆管理

## 🔧 高级配置

### 配置映射功能

Terra-Nova 提供了智能的配置映射功能，当你配置了 `terra.nova.models.providers` 后，会自动映射到 Spring AI 的原生配置。这意味着你只需要配置一套 Terra Nova 配置，就能同时享受 Terra Nova 的增强功能和 Spring AI 的原生支持。

#### 配置映射规则

| Terra Nova 配置 | Spring AI 配置 | 说明 |
|----------------|---------------|------|
| `terra.nova.models.providers.openai.api-key` | `spring.ai.openai.api-key` | OpenAI API 密钥 |
| `terra.nova.models.providers.openai.base-url` | `spring.ai.openai.base-url` | OpenAI API 基础URL |
| `terra.nova.models.providers.anthropic.api-key` | `spring.ai.anthropic.api-key` | Anthropic API 密钥 |
| `terra.nova.models.providers.ollama.base-url` | `spring.ai.ollama.base-url` | Ollama 服务地址 |
| `terra.nova.vector-stores.redis.host` | `spring.ai.vectorstore.redis.uri` | Redis 向量存储配置 |
| `terra.nova.vector-stores.postgresql.url` | `spring.ai.vectorstore.pgvector.url` | PostgreSQL 向量存储配置 |

#### 配置优先级

1. **命令行参数** (最高优先级)
2. **系统属性**
3. **Terra Nova 映射配置** (自动生成)
4. **application.yml/properties** (最低优先级)

这意味着 Terra Nova 的配置会覆盖 `application.yml` 中的 `spring.ai` 配置，但不会覆盖命令行参数或系统属性。

#### 示例配置

```yaml
# 只需要配置 Terra Nova，Spring AI 配置会自动生成
terra:
  nova:
    enabled: true
    spring-ai:
      enabled: true
    models:
      providers:
        openai:
          api-key: ${OPENAI_API_KEY}
          base-url: https://api.openai.com/v1
          models:
            - name: gpt-4o
              type: CHAT
              temperature: 0.7
              max-tokens: 4096
        anthropic:
          api-key: ${ANTHROPIC_API_KEY}
          models:
            - name: claude-3-5-sonnet
              type: CHAT
              temperature: 0.7

# 以下 Spring AI 配置会被自动生成，无需手动配置
# spring:
#   ai:
#     openai:
#       api-key: ${OPENAI_API_KEY}
#       base-url: https://api.openai.com/v1
#       chat:
#         options:
#           model: gpt-4o
#           temperature: 0.7
#           max-tokens: 4096
#     anthropic:
#       api-key: ${ANTHROPIC_API_KEY}
#       chat:
#         options:
#           model: claude-3-5-sonnet
#           temperature: 0.7
```

### 模型混合配置

```yaml
terra:
  nova:
    blending:
      enabled: true
      strategies:
        - name: quality-first
          models: [gpt-4o, claude-3-5-sonnet]
          merge-strategy: BEST_QUALITY
          timeout: 30s
        - name: speed-first
          models: [gpt-3.5-turbo, claude-3-haiku]
          merge-strategy: FASTEST_RESPONSE
          timeout: 10s
```

### 向量数据库配置

```yaml
terra:
  nova:
    vector-stores:
      redis:
        host: localhost
        port: 6379
        index-name: terra-vectors
        distance-metric: COSINE
      postgresql:
        url: jdbc:postgresql://localhost:5432/vectordb
        table-name: vector_store
        dimensions: 1536
```

### 监控和观测配置

```yaml
terra:
  nova:
    observability:
      metrics:
        enabled: true
        export:
          prometheus: true
          cloudwatch: false
      tracing:
        enabled: true
        sampling-rate: 0.1
      logging:
        level: INFO
        include-request-response: false
```

## 📊 监控和指标

Terra-Nova 提供丰富的监控指标：

### 核心指标
- `terra.nova.chat.requests.total` - 聊天请求总数
- `terra.nova.chat.requests.duration` - 请求处理时间
- `terra.nova.chat.tokens.consumed` - Token 消耗量
- `terra.nova.cache.hit.rate` - 缓存命中率
- `terra.nova.model.errors.total` - 模型错误总数

### 业务指标
- `terra.nova.rag.queries.total` - RAG 查询总数
- `terra.nova.rag.retrieval.accuracy` - 检索准确率
- `terra.nova.agent.tasks.completed` - Agent 任务完成数
- `terra.nova.function.calls.total` - 函数调用总数

## 🔒 安全特性

### 内容安全
- 输入内容过滤和验证
- 输出内容审核和清理
- 敏感信息检测和脱敏
- 恶意内容拦截

### 访问控制
- API 密钥管理和轮换
- 用户身份验证和授权
- 速率限制和配额管理
- 审计日志记录

### 数据保护
- 传输加密（TLS 1.3）
- 存储加密（AES-256）
- 密钥管理集成
- 数据脱敏和匿名化

## 🧪 测试支持

### 单元测试
```java
@SpringBootTest
@TestPropertySource(properties = {
    "terra.nova.enabled=true",
    "terra.nova.spring-ai.enabled=true"
})
class TerraChatClientTest {
    
    @Autowired
    private TerraChatClient chatClient;
    
    @Test
    void testBasicChat() {
        String response = chatClient.prompt("Hello")
                .call()
                .content();
        assertThat(response).isNotEmpty();
    }
}
```

### 集成测试
```java
@SpringBootTest
@Testcontainers
class TerraRAGIntegrationTest {
    
    @Container
    static RedisContainer redis = new RedisContainer("redis:7-alpine");
    
    @Test
    void testRAGWorkflow() {
        // 测试完整的 RAG 工作流
    }
}
```

## 📚 示例项目

### 智能客服系统
```java
@Service
public class CustomerServiceBot {
    
    @Autowired
    private TerraChatClient chatClient;
    
    @Autowired
    private TerraRAGService ragService;
    
    public String handleCustomerQuery(String query, String customerId) {
        // 1. 检索相关知识
        String context = ragService.retrieveContext(query);
        
        // 2. 构建提示词
        String prompt = buildPrompt(query, context, customerId);
        
        // 3. 生成回复
        return chatClient.prompt(prompt)
                .advisors(new CustomerServiceAdvisor())
                .call()
                .content();
    }
}
```

### 文档分析系统
```java
@Service
public class DocumentAnalyzer {
    
    @Autowired
    private TerraAgentSystem agentSystem;
    
    public AnalysisReport analyzeDocument(MultipartFile file) {
        return agentSystem.createAgent(AgentType.DOCUMENT_ANALYZER)
                .withTools(List.of("pdf-reader", "text-analyzer", "summarizer"))
                .execute("分析上传的文档并生成报告", 
                        Map.of("document", file));
    }
}
```

## 🚀 性能优化

### 缓存策略
- **语义缓存**: 基于向量相似度的智能缓存
- **结果缓存**: LRU 策略的结果缓存
- **模型缓存**: 模型响应的分层缓存

### 并发优化
- **异步处理**: 非阻塞的异步调用
- **连接池**: 优化的 HTTP 连接池
- **批处理**: 批量请求处理优化

### 资源管理
- **内存管理**: 智能的内存使用优化
- **连接管理**: 自动的连接生命周期管理
- **线程池**: 可配置的线程池策略

## 🧪 测试和验证

### 运行测试
```bash
# 运行所有测试
mvn test

# 运行集成测试
mvn test -Dtest=TerraNovaIntegrationTest

# 运行配置测试
mvn test -Dtest=TerraNovaAutoConfigurationTest
```

### 验证集成
```java
@Autowired
private SpringAIIntegrationManager integrationManager;

// 检查集成状态
boolean isHealthy = integrationManager.isIntegrationHealthy();
Map<String, Object> status = integrationManager.getIntegrationStatus();
String diagnostic = integrationManager.getDiagnosticInfo();
```

## 🔄 版本兼容性

| Terra-Nova 版本 | Spring AI 版本 | Spring Boot 版本 | Java 版本 |
|----------------|---------------|-----------------|----------|
| 0.0.1-SNAPSHOT | 1.1.0         | 3.2.x+          | 17+      |
| 0.1.0          | 1.1.x         | 3.2.x+          | 17+      |
| 0.2.0          | 1.1.x         | 3.3.x+          | 17+      |

## 🔄 Spring AI 1.1.0 升级说明

Terra-Nova 现已升级到 Spring AI 1.1.0，享受最新特性：

### 新增功能
- **ChatClient API**: 更简洁的聊天客户端接口
- **增强的 Function Calling**: 更好的工具调用支持
- **改进的向量存储**: 更高效的向量数据库集成
- **更好的可观测性**: 增强的监控和追踪能力

### 兼容性说明
- 完全向后兼容 Spring AI 1.0.x 的 API
- 自动适配新的包结构和类名
- 保持现有配置的兼容性

## 📋 实现状态

### ✅ 已完成
- [x] 核心配置属性类 (`TerraNovaProperties`)
- [x] 模型管理器 (`TerraModelManager` 及其实现)
- [x] Spring AI 桥接器 (`TerraToSpringAIBridge`)
- [x] 集成管理器 (`SpringAIIntegrationManager`)
- [x] 聊天客户端 (`TerraChatClient` 及其实现)
- [x] RAG 服务 (`TerraRAGService` 及其实现)
- [x] Spring Boot 自动配置 (`TerraNovaAutoConfiguration`)
- [x] 配置元数据文件 (IDE 支持)
- [x] 示例配置文件
- [x] 基础测试用例
- [x] Maven 依赖配置
- [x] Spring AI 1.1.0 升级适配

### 🚧 待完善
- [ ] Agent 系统实现
- [ ] 模型混合策略实现
- [ ] 高级缓存机制
- [ ] 监控指标收集
- [ ] 安全防护机制
- [ ] 更多测试用例
- [ ] 性能基准测试

## 🤝 贡献指南

我们欢迎社区贡献！请查看 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解详细信息。

### 开发环境设置
1. 克隆仓库
2. 安装 JDK 17+
3. 运行 `mvn clean install`
4. 配置 IDE 导入项目

### 提交规范
- 遵循 Conventional Commits 规范
- 包含完整的测试用例
- 更新相关文档

## 📄 许可证

本项目采用 [Apache License 2.0](../LICENSE) 许可证。

## 🆘 支持和帮助

- **文档**: [Terra Framework 官方文档](https://terra-framework.com/docs)
- **问题反馈**: [GitHub Issues](https://github.com/terra-framework/terra-framework/issues)
- **讨论社区**: [GitHub Discussions](https://github.com/terra-framework/terra-framework/discussions)
- **技术支持**: support@terra-framework.com

---

**Terra-Nova** - 让 AI 集成变得简单而强大 🚀
