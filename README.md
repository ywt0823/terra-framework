# Terra Framework

Terra Framework is a collection of modules designed to accelerate the development of Spring Boot applications.

## Modules

-   **terra-bedrock**: Core functionalities, base application annotations, and foundational configurations.
-   **terra-crust**: Web layer enhancements, including request tracing, logging filters, and unified response handling.
-   **terra-sediment**: Common utilities, exceptions, and data structures.
-   **terra-geyser**: Provides a powerful, multi-level (local + Redis) caching framework. It can be used as a standalone caching solution. When used with `terra-strata`, it automatically enhances database operations with caching.
-   **terra-strata**: A dedicated data access layer module, providing helpers and configurations for database operations (e.g., using MyBatis). It is fully independent and does not contain any caching logic itself.
-   **terra-stream**: Messaging module, currently supporting RabbitMQ.
-   **terra-nova**: LLM and AI-related functionalities.

## Architecture: Caching Enhancement

The `terra-geyser` and `terra-strata` modules are designed to be decoupled but work together seamlessly.

-   **Standalone `terra-geyser`**: Provides `@AutoCache` annotation and a multi-level caching mechanism.
-   **Standalone `terra-strata`**: Provides database operation helpers.
-   **`terra-geyser` + `terra-strata`**: When both modules are on the classpath, `terra-geyser`'s `AutoCacheAspect` automatically provides caching for any method annotated with `@AutoCache`. Furthermore, `terra-strata` provides additional enhancers like `SqlAutoCacheAspect` which can automatically cache frequently accessed SQL queries (hotspot detection) and `CacheInvalidationAspect` which can automatically clear caches when underlying table data changes.

This architecture ensures a clean separation of concerns, where the data access layer is not aware of the caching implementation, and the caching layer can dynamically enhance the data layer if it is present.

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

4. **Agent系统与工具**
   - 多种Agent类型支持（ReAct、Plan-and-Execute）
   - 工具注册与管理系统
   - 基于注解的工具开发
   - 内置常用工具集（计算器、日期时间处理、文本分析等）
   - 记忆管理和上下文维护

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

## 7. RAG检索增强生成系统

Terra Framework提供了完整的检索增强生成(RAG)支持，通过RAG系统，您可以将大型文档库集成到LLM应用中，实现知识增强型的生成能力。

### 7.1 核心功能

- **文档处理**：加载、解析和分割各种格式的文档
- **嵌入生成**：将文档转换为向量表示
- **向量存储**：高效存储和检索文档向量
- **相似度搜索**：根据用户查询检索相关文档
- **上下文构建**：根据检索结果构建LLM输入上下文
- **灵活配置**：支持自定义分块大小、检索参数等

### 7.2 快速开始

#### 添加依赖

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-nova-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

#### 配置示例

在`application.yml`中添加:

```yaml
terra:
  nova:
    rag:
      enabled: true
      # 文档分割配置
      splitting:
        chunk-size: 1000
        overlap: 200
      # 检索配置
      retrieval:
        top-k: 5
        minimum-score: 0.7
      # 嵌入配置
      embedding:
        model-id: deepseek:text-embedding
        dimension: 1024
      # 向量存储配置
      vector-store:
        type: in-memory
        collection-name: documents
```

#### 使用示例

```java
@Service
public class DocumentService {

    @Autowired
    private RAGService ragService;

    // 加载文档到知识库
    public void loadDocuments(String directory) {
        try {
            int docCount = ragService.loadDocuments(directory);
            log.info("成功加载{}个文档", docCount);
        } catch (DocumentLoadException e) {
            log.error("文档加载失败", e);
        }
    }
    
    // 根据用户问题检索相关文档
    public String answerQuestion(String question) {
        // 生成包含相关文档的上下文
        String context = ragService.generateContext(question, 3);
        
        // 将上下文和问题一起发送给LLM
        return aiService.chat("请根据以下上下文回答问题:\n\n" + context);
    }
}
```

### 7.3 自定义组件

Terra的RAG系统支持自定义各个组件:

```java
// 自定义文档加载器
@Component
public class PDFDocumentLoader implements DocumentLoader {
    
    @Override
    public List<Document> loadDocuments(String source) {
        // 实现PDF文件加载逻辑
    }
}

// 注册自定义加载器
@Configuration
public class RAGConfig {
    
    @Autowired
    private RAGService ragService;
    
    @Autowired
    private PDFDocumentLoader pdfLoader;
    
    @PostConstruct
    public void setup() {
        // 为RAGService设置文档加载器
        if (ragService instanceof DefaultRAGService) {
            ((DefaultRAGService) ragService).setDocumentLoader(pdfLoader);
        }
    }
}
```

### 7.4 配置详解

#### 文档分割配置

```yaml
terra:
  nova:
    rag:
      splitting:
        # 每个文档块的大小（按字符数计算）
        chunk-size: 1000
        # 块之间的重叠大小（按字符数计算）
        overlap: 200
        # 分割器类型: character, token, sentence, paragraph
        splitter: character
```

#### 检索配置

```yaml
terra:
  nova:
    rag:
      retrieval:
        # 检索结果数量
        top-k: 5
        # 是否启用重排序
        rerank: false
        # 重排序模型ID（如启用）
        rerank-model: ""
        # 最低相似度阈值
        minimum-score: 0.7
```

#### 嵌入配置

```yaml
terra:
  nova:
    rag:
      embedding:
        # 使用的嵌入模型ID
        model-id: deepseek:text-embedding
        # 嵌入向量维度
        dimension: 1024
        # 批处理大小
        batch-size: 20
        # 是否启用嵌入缓存
        cache-enabled: true
```

### 7.5 最佳实践

1. **文档分割**：根据文档类型调整分块大小，对于结构化文档，保持段落、章节完整
2. **检索优化**：通过调整top-k和最低相似度阈值平衡召回率和准确率
3. **模型选择**：对于大型知识库，使用高维度嵌入模型提高表示能力
4. **上下文管理**：监控上下文长度，避免超出LLM模型最大输入限制
5. **缓存利用**：启用嵌入缓存提高性能，特别是对于频繁检索的文档 

## 8. Agent工具注解开发指南

### 8.1 简介

Terra-Nova框架提供了两种注册Agent工具的方式：
1. 实现`Tool`接口并使用`@Component`注解（传统方式）
2. 使用`@AITool`注解在任何Spring Bean方法上（注解方式）

本指南主要介绍第二种注解方式的使用方法，这种方式无需创建专门的工具类，可以直接将现有业务方法转变为Agent工具。

### 8.2 注解工具的优势

- **简化开发**：无需创建单独的工具类，直接注解现有方法
- **轻松集成**：将业务逻辑与Agent工具能力无缝结合
- **职责分离**：业务服务专注于功能实现，而不是工具适配
- **减少样板代码**：不必手动实现Tool接口的所有方法

### 8.3 使用方法

#### 步骤1：添加方法注解

在任何Spring托管的Bean中，为方法添加`@AITool`注解：

```java
@Service
public class WeatherService {

    @AITool(
        name = "weather_query",
        description = "查询指定城市的天气信息"
    )
    public String getWeather(
            @AIToolParameter(name = "city", description = "城市名称") String city,
            @AIToolParameter(name = "forecast", description = "是否查询未来几天的天气", required = false) boolean forecast) {
        // 方法实现...
    }
}
```

#### 步骤2：添加参数注解

为方法参数添加`@AIToolParameter`注解，提供参数描述：

```java
@AIToolParameter(
    name = "paramName",     // 参数名称
    description = "参数描述", // 参数描述
    type = "string",        // 参数类型（可选，默认根据Java类型推断）
    required = true,        // 是否必需（可选，默认为true）
    defaultValue = ""       // 默认值（可选，仅用于非必需参数）
)
```

#### 8.3.1 快速开始

在项目中使用注解式工具：

```java
// 1. 创建带注解的工具类
@Service
public class DefaultTools {

    @AITool(
        name = "calculator",
        description = "执行基本数学运算，包括加减乘除和指数"
    )
    public Object calculator(
            @AIToolParameter(name = "operation", description = "操作类型，支持：add, subtract, multiply, divide, power") String operation,
            @AIToolParameter(name = "a", description = "第一个操作数") Object a,
            @AIToolParameter(name = "b", description = "第二个操作数") Object b) {
        // 实现计算逻辑...
    }
    
    @AITool(
        name = "datetime",
        description = "处理日期时间相关操作，包括当前时间、日期计算等"
    )
    public String datetimeTool(
            @AIToolParameter(name = "operation", description = "操作类型(now/parse/format/add/subtract等)") String operation,
            @AIToolParameter(name = "date", description = "日期字符串", required = false) String date) {
        // 实现日期处理逻辑...
    }
}

// 2. 在Agent中使用工具
@Service
public class MyApplicationService {

    @Autowired
    private AgentFactory agentFactory;
    
    public String processUserQuery(String query) {
        // 创建Agent配置，包含要使用的工具
        AgentConfig config = DefaultAgentConfig.builder()
            .modelId("gpt-4")
            .addTool("calculator") 
            .addTool("datetime")
            .build();
            
        // 创建Agent实例
        Agent agent = agentFactory.createAgent(AgentType.REACT, config);
        
        // 执行Agent任务
        AgentResponse response = agent.execute(query, null);
        
        return response.getOutput();
    }
}
```

配置自动工具扫描：

```yaml
terra:
  nova:
    agent:
      enabled: true
      tool:
        auto-register: true
        base-packages: com.example.myapp
```

### 8.4 注解说明

#### @AITool

| 属性 | 类型 | 必需 | 描述 |
|-----|-----|------|-----|
| name | String | 否 | 工具名称，如果未指定则使用方法名 |
| description | String | 是 | 工具描述 |

#### @AIToolParameter

| 属性 | 类型 | 必需 | 描述 |
|-----|-----|------|-----|
| name | String | 否 | 参数名称，如果未指定则使用方法参数名 |
| description | String | 是 | 参数描述 |
| type | String | 否 | 参数类型，如果未指定则根据Java类型推断 |
| required | boolean | 否 | 是否为必需参数，默认为true |
| defaultValue | String | 否 | 默认值，仅用于非必需参数 |

### 8.5 类型映射

Java类型会自动映射为工具参数类型：

| Java类型 | 工具参数类型 |
|---------|------------|
| String | string |
| Integer, int, Long, long | number |
| Double, double, Float, float | number |
| Boolean, boolean | boolean |
| 数组, List | array |
| 其他类型 | object |

### 8.6 示例

#### 基本示例

```java
@Service
public class UtilityService {

    @AITool(description = "获取当前时间")
    public String getCurrentTime(
            @AIToolParameter(description = "时间格式", required = false, defaultValue = "yyyy-MM-dd HH:mm:ss") String format) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(format);
        return LocalDateTime.now().format(formatter);
    }
}
```

#### 复杂示例

```java
@Service
public class TextService {

    @AITool(
        name = "text_analyzer",
        description = "分析文本，包括字数统计、关键词提取等"
    )
    public Map<String, Object> analyzeText(
            @AIToolParameter(description = "要分析的文本") String text,
            @AIToolParameter(description = "分析类型", defaultValue = "all") String type,
            @AIToolParameter(description = "是否包含详细信息", required = false) Boolean detailed) {
        
        // 实现文本分析逻辑...
        Map<String, Object> result = new HashMap<>();
        // ...填充结果
        return result;
    }
}
```

### 8.7 最佳实践

1. **提供清晰描述**：为工具和每个参数提供详细、准确的描述
2. **使用合适的类型**：选择最合适的参数类型，帮助Agent正确使用工具
3. **设置合理默认值**：为非必需参数提供合理的默认值
4. **保持方法简单**：一个工具应专注于一个特定功能
5. **处理异常**：妥善处理异常情况，返回友好的错误信息
6. **日志记录**：添加适当的日志记录，方便调试和监控

### 8.8 配置

在`application.yml`或`application.properties`中可以设置扫描包路径：

```yaml
terra:
  nova:
    agent:
      tool:
        base-packages: com.example.myapp
```

### 8.9 故障排除

如果注解工具未被正确注册，请检查：

1. 确保类被Spring正确管理（@Component、@Service等）
2. 验证方法是否为public
3. 检查扫描包路径配置是否正确
4. 查看应用日志中是否有相关错误信息 

## 9. 统一AI组件系统

Terra-Nova框架现在提供了统一的AI组件系统，允许开发者使用同一套注解创建同时支持Agent工具和LLM函数调用的组件。

> **重要通知**: 传统的`@AITool`、`@AIToolParameter`和`@AIFunction`、`@AIParameter`注解系统已被完全移除。
> 只能使用新的统一`@AIComponent`和`@AIParameter`注解系统进行开发。详见[统一AI组件开发指南](terra-nova/doc/agent/unified-component-guide.md)。

### 9.1 核心特性

- **统一注解**：使用`@AIComponent`和`@AIParameter`注解标记方法和参数
- **灵活配置**：通过`types`属性指定组件类型（Tool、Function或两者都是）
- **简化开发**：一次开发，双重用途，减少代码冗余

### 9.2 迁移指南

如果你之前使用了旧的`@AITool`或`@AIFunction`注解，现在必须迁移到新的`@AIComponent`注解：

1. 将`@AITool`或`@AIFunction`替换为`@AIComponent`
2. 将`@AIToolParameter`或原函数的`@AIParameter`替换为新的`@AIParameter`
3. 根据需要设置`types`属性指定使用场景

例如，从：
```java
@AITool(name = "calc", description = "计算器")
public double calculate(
    @AIToolParameter(description = "表达式") String expr
) { ... }
```

迁移到：
```java
@AIComponent(name = "calc", description = "计算器")
public double calculate(
    @AIParameter(description = "表达式") String expr
) { ... }
```

更多详细信息请参考[统一AI组件开发指南](terra-nova/doc/agent/unified-component-guide.md)。 