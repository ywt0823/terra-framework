# Terra Nova

Terra Nova 是 Terra Framework 的核心子项目，专注于大语言模型(LLM)的统一接入与增强。它提供了一套完整的工具集，用于模型管理、多模型混合调用、函数调用、对话管理和提示词模板等AI服务增强功能。

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
- **Coze**：支持 Coze 平台模型

主要特性：
- 统一的模型接口与请求格式
- 灵活的认证机制（API Key、Bearer Token、AK/SK 等）
- 模型状态监控与自动重试
- 模型信息注册与管理
- 同步/异步/流式生成支持

### 2. 模型混合系统（Model Blender）

Terra Nova 提供强大的模型混合能力，可以同时调用多个 LLM 模型并智能合并结果：

- **多策略支持**：
  - WEIGHTED：根据配置的权重对多个模型响应进行合并
  - LONGEST：选择最长的响应
  - SHORTEST：选择最短的响应
  - VOTING：多模型结果投票决定最终输出
  - CONCATENATE：串联所有结果
  - RANDOM：随机选择一个结果
  - FIRST_SUCCESS：使用第一个成功的结果
  - QUALITY_BASED：选择质量最高的响应
  - LIST_FORMAT：合并为列表（每个模型的结果作为一个列表项）
  - INTERLEAVE：交错合并（交替使用每个模型的部分结果）

- **结果合并机制**：
  - 智能文本合并
  - 令牌使用统计合并
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
  - 键值管理和过期清理

- **请求监控**：
  - 响应时间监控
  - 令牌使用统计
  - 错误率追踪
  - 重试次数记录

### 4. 对话管理系统

Terra Nova 提供完整的对话管理系统，用于追踪和管理用户与 AI 的对话：

- **对话存储**：
  - 内存存储实现
  - 对话和消息的结构化存储
  - 用户关联的对话管理

- **对话服务**：
  - 创建会话和关联用户
  - 添加消息到会话
  - 获取单个会话和用户所有会话
  - 归档和删除会话

- **对话感知 AI 服务**：
  - 自动记录对话上下文
  - 关联模型响应与对话
  - 会话状态管理（活跃、归档、删除）

### 5. AI 函数调用系统

Terra Nova 实现了完善的函数调用框架，使 AI 模型能够调用应用内的函数：

- **函数注册与发现**：
  - 注解驱动的函数注册（@AIFunction）
  - 自动扫描与加载函数
  - 函数注册表管理

- **函数参数定义**：
  - 注解驱动的参数定义（@AIParameter）
  - 参数类型推断和验证
  - 必需参数校验

- **函数执行**：
  - 同步和异步执行支持
  - 参数映射和类型转换
  - 异常处理机制

- **函数格式适配**：
  - 不同模型的函数格式转换
  - 响应解析和函数调用提取

### 6. 提示词模板系统

Terra Nova 提供了强大的提示词模板管理系统：

- **模板加载**：
  - 文件系统模板加载
  - HTTP远程模板加载
  - 模板缓存管理

- **模板渲染**：
  - 变量插值支持
  - 高效模板渲染
  - 可扩展的模板引擎

- **提示词服务**：
  - 统一的模板渲染接口
  - 提示词创建和管理
  - 缓存支持提高性能

- **配置化支持**：
  - 模板路径配置
  - 文件扩展名配置
  - 缓存大小和过期时间配置

### 7. RAG 检索增强生成系统

Terra Nova 提供了完整的RAG（Retrieval-Augmented Generation，检索增强生成）解决方案，通过将文档知识库与大型语言模型结合，生成更加准确、可靠的回答，减少幻觉问题：

- **文档处理模块**：
  - Document：文档接口与SimpleDocument实现
  - EnhancedDocument：增强型文档实现，支持更丰富的元数据
  - DocumentLoader：文档加载器接口与多种实现（文本、PDF、HTML、URL等）
  - DocumentProcessor：文档处理器接口与归一化、去重等实现
  - DocumentSplitter：文档分割器接口与递归字符分割、语义分割等实现
  - DocumentLoaderFactory：文档加载器工厂，智能选择合适的加载器
  - DocumentProcessorChain：文档处理链，支持多处理器顺序应用

- **嵌入模块**：
  - EmbeddingModel：嵌入模型接口
  - EmbeddingService：嵌入服务接口与默认实现
  - 多种嵌入模型支持与缓存

- **向量存储**：
  - VectorStore：向量存储接口
  - InMemoryVectorStore：内存向量存储实现
  - SearchResult：检索结果封装

- **检索模块**：
  - Retriever：检索器接口与默认实现
  - RetrievalOptions：检索参数配置
  - 相似度过滤与重排序支持

- **重排序模块**：
  - Reranker：重排序器接口与抽象实现
  - CrossEncoderReranker：基于交叉编码器的高效实现
  - LLMReranker：基于大语言模型的灵活实现，默认使用DeepSeek模型
  - 多级过滤与精准排序机制

- **上下文构建**：
  - ContextBuilder：上下文构建器接口
  - 自定义模板支持
  - 文档格式化

- **服务模块**：
  - RAGService：统一服务接口
  - 文档加载与管理
  - 知识库检索与上下文生成

#### RAG 文档重排序功能

Terra框架RAG模块的文档重排序(Rerank)功能可以显著提升检索质量，通过在初始向量相似度检索后进行更精准的文档排序：

**重排序工作流程**：

1. 首先通过向量相似度检索出初步的候选文档
2. 然后使用更精确的语义匹配模型对这些文档进行重新评分和排序
3. 根据新的分数对文档进行排序，过滤低质量的匹配结果

**重排序策略**：

- **交叉编码器(Cross-Encoder)**：使用专门的相似度评分模型，适合短文本的快速评分，精度高效率好
- **LLM重排序**：使用大语言模型进行评分，更灵活但计算开销较大，适合需要复杂判断的场景

#### 增强文档处理功能

Terra框架支持多种文档类型的处理和加载，提供了统一的接口和丰富的实现：

**主要特性**：

1. **多格式文档支持**：
   - 文本文件：TXT、CSV、JSON、XML等
   - 富文本文档：PDF、Word、HTML、Markdown等
   - 网络资源：URL链接、网页内容
   - 图像文档：带OCR功能的图像处理（可选）

2. **智能文档加载**：
   - 自动识别文件类型
   - 选择适合的加载器
   - 提取丰富的元数据
   - 处理复杂的文档结构

3. **高级语义分割**：
   - 基于语义单元的智能分割
   - 保留段落和句子的完整性
   - 自定义分隔符和分割策略
   - 避免语义截断问题

4. **文档处理链**：
   - 组合多个处理器顺序应用
   - 文本归一化和清洗
   - 内容去重和过滤
   - 性能监控和错误处理

**配置示例**：

```yaml
# 文档处理配置示例
terra:
  nova:
    rag:
      document:
        # 基本配置
        enabled: true
        default-chunk-size: 1000
        default-chunk-overlap: 200
        default-splitting-strategy: semantic
        
        # 加载器配置
        loaders:
          pdf:
            extract-tables: true
            extract-metadata: true
          html:
            strip-tags: true
          url:
            follow-redirects: true
            read-timeout: 30000
        
        # 分割器配置
        splitters:
          semantic:
            respect-paragraphs: true
            sentence-separators: ['.', '!', '?', '。', '！', '？', '\n']
          
        # 处理器配置
        processors:
          remove-extra-whitespace: true
          enable-duplicate-removal: true
```

**使用示例**：

```java
// 使用增强文档处理加载和分割文档
@Autowired
private DocumentLoaderFactory loaderFactory;

@Autowired
private SemanticTextSplitter semanticSplitter;

public List<Document> loadAndSplitDocument(String filePath) {
    // 根据文件类型获取合适的加载器
    DocumentLoader loader = loaderFactory.getLoaderForFile(filePath);
    
    // 加载文档
    List<Document> documents = loader.loadDocuments(filePath);
    
    // 使用语义分割器分割文档
    List<Document> chunks = new ArrayList<>();
    for (Document doc : documents) {
        chunks.addAll(semanticSplitter.split(doc));
    }
    
    return chunks;
}
```

### 8. 智能代理系统

Terra Nova 提供了完整的智能代理（Agent）系统，可以通过结合大语言模型和工具来自动完成复杂任务：

- **代理实现**：
  - ReAct 代理：基于思考-行动-观察循环的代理实现
  - PlanAndExecute 代理：基于先规划后执行的代理实现
  - 支持自定义代理类型扩展

- **工具管理**：
  - 工具注册与发现
  - 可扩展的工具接口
  - 自动扫描注册工具

- **代理监控**：
  - 执行跟踪
  - 执行步骤记录
  - 工具使用统计
  - 执行时间统计

- **记忆管理**：
  - 状态持久化
  - 上下文记忆
  - 短期/长期记忆

- **集成功能**：
  - 与对话系统集成
  - 与提示词模板系统集成
  - 与函数调用系统集成

#### 代理系统架构

代理系统由以下核心组件组成：

1. **Agent接口**：定义代理的基本行为
2. **AgentConfig**：代理配置接口
3. **AgentFactory**：代理工厂，创建不同类型的代理
4. **Tool**：工具接口，定义代理可以使用的工具
5. **ToolRegistry**：工具注册表，管理可用工具
6. **MemoryManager**：记忆管理器，管理代理的记忆

#### 使用示例

```java
// 创建代理工厂
@Autowired
private AgentFactory agentFactory;

// 使用默认配置创建ReAct代理
Agent reactAgent = agentFactory.createAgent(AgentType.REACT);

// 执行任务
AgentResponse response = reactAgent.execute(
    "查找客户John Doe的最近订单并计算总金额",
    Map.of("customerId", "JD123")
);

// 输出执行结果
System.out.println("执行结果: " + response.getOutput());
System.out.println("工具使用统计: " + response.getToolUsage());
System.out.println("执行时间: " + response.getMetadata().get("taskTimeMs") + "ms");

// 获取详细执行痕迹
AgentExecutionTrace trace = reactAgent.executeWithTrace(
    "为产品A、B、C创建营销计划",
    Map.of("budget", 10000)
);

// 分析执行步骤
for (AgentStep step : trace.getSteps()) {
    System.out.println("步骤 " + step.getId());
    System.out.println("思考: " + step.getThought());
    System.out.println("动作: " + step.getAction().getType());
    System.out.println("结果: " + step.getActionResult());
}

// 自定义代理配置
DefaultAgentConfig config = new DefaultAgentConfig();
config.setModelId("gpt-4");
config.setMaxSteps(15);
config.setTimeoutMs(120000);
config.setTools(List.of("search", "calculator", "database"));

// 创建Plan-and-Execute代理
Agent planAgent = agentFactory.createAgent(AgentType.PLAN_AND_EXECUTE, config);
```

#### 配置示例

```yaml
# 代理系统配置
terra:
  nova:
    agent:
      enabled: true
      default-type: REACT
      default-model-id: gpt-4
      max-steps: 10
      timeout-ms: 60000
      thread-pool-size: 10
      base-packages: com.mycompany.tools
      
      # 工具配置
      tool:
        auto-register: true
        base-packages: com.mycompany.tools
        timeout-ms: 30000
      
      # 对话集成
      conversation:
        enabled: true
        record-steps: false
      
      # ReAct代理配置
      react:
        prompt-template-id: agent.react
        backtracking-enabled: true
        backtracking-depth: 3
      
      # Plan-and-Execute代理配置
      plan-and-execute:
        planner-prompt-template-id: agent.planner
        executor-prompt-template-id: agent.executor
        max-plan-steps: 5
```

#### 自定义工具实现

```java
@Component
public class WeatherTool implements Tool {
    
    @Override
    public String getName() {
        return "weather";
    }
    
    @Override
    public String getDescription() {
        return "获取指定城市的天气信息";
    }
    
    @Override
    public List<ToolParameter> getParameters() {
        return List.of(
            new ToolParameter("city", "string", "城市名称", true),
            new ToolParameter("date", "string", "日期，格式YYYY-MM-DD", false)
        );
    }
    
    @Override
    public Object execute(Map<String, Object> parameters) {
        String city = (String) parameters.get("city");
        String date = (String) parameters.getOrDefault("date", "today");
        
        // 实际实现应调用天气API
        return String.format("%s 天气：晴，温度20-28℃，湿度60%%", city);
    }
}
```

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
  framework:
    ai:
      enabled: true
      default-model-id: openai:gpt-3.5-turbo
      models:
        gpt-3.5-turbo:
          type: openai
          api-key: ${OPENAI_API_KEY}
          endpoint: https://api.openai.com/v1
        claude-3-haiku:
          type: claude
          api-key: ${ANTHROPIC_API_KEY}
          endpoint: https://api.anthropic.com
        wenxin-model:
          type: wenxin
          api-key-id: ${BAIDU_API_KEY}
          api-key-secret: ${BAIDU_SECRET_KEY}
        tongyi-model:
          type: tongyi
          api-key: ${TONGYI_API_KEY}
  nova:
    # RAG检索增强生成配置
    rag:
      enabled: true
      # 文档分割配置
      splitting:
        chunk-size: 1000
        overlap: 200
        splitter: character
      # 检索配置
      retrieval:
        top-k: 5
        rerank: false
        rerank-model: ""
        minimum-score: 0.7
      # 上下文构建配置
      context:
        template: |
          根据以下上下文回答问题:
          
          {context}
          
          问题: {question}
        max-tokens: 3500
        format-documents: true
        document-template: "文档[{index}]: {content}\n来源: {source}"
      # 向量存储配置
      vector-store:
        type: in-memory
    
    # 嵌入服务配置
    rag.embedding:
      enabled: true
      model-id: openai:text-embedding-ada-002
      dimension: 1536
      batch-size: 20
      cache-enabled: true
      cache-size: 1000
      cache-ttl-seconds: 3600
      
    blend:
      enabled: true
      merge-strategy: WEIGHTED
      auto-add-models: true
    retry:
      enabled: true
      max-retries: 3
      initial-delay-ms: 1000
      max-delay-ms: 10000
      backoff-multiplier: 2.0
    cache:
      enabled: true
      default-ttl-seconds: 3600
    monitoring:
      enabled: true
    conversation:
      enabled: true
      max-messages-per-conversation: 100
    function:
      enabled: true
      base-packages: com.example.functions
    prompt:
      template-path: classpath:/prompts
      template-extension: .prompt
      cache:
        enabled: true
        ttl-seconds: 3600
        max-size: 1000
```

## 使用示例

### 基础 AI 服务

```java
@Autowired
private AIService aiService;

// 简单文本生成
String response = aiService.generateText("你好，请介绍一下自己");

// 对话生成
List<Message> messages = new ArrayList<>();
messages.add(Message.ofSystem("你是一个友好的AI助手"));
messages.add(Message.ofUser("介绍一下自己"));
String response = aiService.chat(messages);

// 使用参数
Map<String, Object> parameters = new HashMap<>();
parameters.put("temperature", 0.7);
parameters.put("top_p", 0.95);
String response = aiService.generateText("分析这段代码", parameters);

// 流式响应
Publisher<String> stream = aiService.generateTextStream("讲一个故事");
stream.subscribe(new Subscriber<>() {
    @Override
    public void onNext(String chunk) {
        System.out.print(chunk);
    }
    // 其他方法实现...
});
```

### 增强型 AI 服务

```java
@Autowired
private EnhancedAIService enhancedService;

// 使用特定模型
AIModel model = enhancedService.getModel("openai", "gpt-4");
ModelResponse response = model.generate("分析这段文本", parameters);

// 模型混合调用
String blendedResponse = enhancedService.generateTextWithBlending("解释量子计算的基本原理");

// 添加模型到混合器
enhancedService.addModelToBlender("gpt-4", 60);
enhancedService.addModelToBlender("claude-3-sonnet", 40);

// 自定义混合策略
ModelBlender blender = enhancedService.getModelBlender();
blender.setMergeStrategy(MergeStrategy.QUALITY_BASED);
```

### 对话管理

```java
@Autowired
private ConversationService conversationService;

// 创建新会话
Conversation conversation = conversationService.createConversation("user123", "技术咨询");

// 添加消息到会话
ConversationMessage userMessage = ConversationMessage.builder()
    .role(MessageRole.USER)
    .content("如何使用Spring Boot?")
    .build();
conversationService.addMessage(conversation.getId(), userMessage);

// 获取用户的所有会话
List<Conversation> userConversations = conversationService.getUserConversations("user123");

// 使用会话感知AI服务
@Autowired
private EnhancedAIService aiService;

// 传入会话ID参数
Map<String, Object> params = Map.of("conversation_id", conversation.getId());
String response = aiService.chat(messages, params);
```

### 函数调用

```java
// 定义函数
@Component
public class WeatherService {
    @AIFunction(
        name = "get_weather",
        description = "获取指定城市的天气信息"
    )
    public Map<String, Object> getWeather(
        @AIParameter(name = "city", description = "城市名称", required = true)
        String city,
        @AIParameter(name = "days", description = "天数预报", required = false)
        Integer days
    ) {
        // 函数实现...
        return Map.of("city", city, "temperature", 25, "condition", "晴天");
    }
}

// 使用函数调用服务
@Autowired
private FunctionCallingService functionService;

// 获取所有函数
List<Function> functions = functionService.getFunctionsForModel("gpt-4");

// 创建带函数的请求
ModelRequest request = ModelRequest.builder()
    .addUserMessage("北京明天天气怎么样？")
    .build();

// 执行带函数的请求
ModelResponse response = functionService.executeWithFunctions(request, functions);

// 处理函数调用
FunctionCall call = functionService.extractFunctionCall(response);
if (call != null) {
    Object result = functionService.executeFunctionCall(call);
    // 处理结果...
}
```

### 提示词模板

```java
@Autowired
private PromptService promptService;

// 渲染模板
Map<String, Object> variables = new HashMap<>();
variables.put("topic", "人工智能");
variables.put("audience", "技术爱好者");
String content = promptService.render("article_intro", variables);

// 创建提示词
Prompt prompt = promptService.createPrompt("weather_report", Map.of(
    "city", "上海",
    "date", "2023-09-10"
));

// 使用提示词生成内容
String result = aiService.generateText(prompt.getContent());
```

### RAG检索增强生成

```java
@Autowired
private RAGService ragService;

@Autowired
private AIService aiService;

// 添加文档到知识库
public void addDocument(String content, Map<String, Object> metadata) {
    Document document = SimpleDocument.builder()
            .content(content)
            .metadata(metadata)
            .build();
    ragService.addDocument(document);
}

// 从文件加载文档
public void loadDocuments(String filePath) throws DocumentLoadException {
    DocumentLoader loader = new TextFileDocumentLoader();
    List<Document> documents = loader.loadDocuments(filePath);
    ragService.addDocuments(documents);
}

// 基于RAG进行问答
public String answer(String question) {
    // 生成包含相关文档的上下文
    String context = ragService.generateContext(question, 5);
    
    // 使用生成的上下文回答问题
    return aiService.generateText(context);
}

// 使用过滤条件检索
RetrievalOptions options = RetrievalOptions.builder()
        .topK(10)
        .addFilter("category", "技术文档")
        .addFilter("language", "中文")
        .build();

List<Document> documents = ragService.retrieve(query, options);
```

## 模型注册与管理

Terra Nova 允许动态注册和管理模型：

```java
@Autowired
private AIModelManager modelManager;

// 注册新模型
ModelConfig config = ModelConfig.builder()
    .modelId("custom-gpt")
    .modelType(ModelType.OPENAI)
    .endpoint("https://custom-api.example.com/v1")
    .authConfig(AuthConfig.ofApiKey("your-api-key"))
    .defaultParameter("temperature", 0.7)
    .timeout(30000)
    .streamSupport(true)
    .build();

modelManager.registerConfig("custom-gpt", config);

// 获取模型实例
AIModel model = modelManager.getModel("custom-gpt");

// 刷新模型
modelManager.refreshModel("custom-gpt");
```

## 高级配置

### 自定义 Auth Provider

```java
public class CustomAuthProvider implements AuthProvider {
    private final AuthConfig authConfig;
    
    public CustomAuthProvider(AuthConfig authConfig) {
        this.authConfig = authConfig;
    }
    
    @Override
    public AuthCredentials getCredentials() {
        // 实现自定义的认证逻辑
        return AuthCredentials.builder()
            .authType(AuthType.API_KEY)
            .headerName("X-Custom-Auth")
            .headerValue(authConfig.getApiKey())
            .build();
    }
    
    @Override
    public void refreshCredentials() {
        // 刷新认证信息
    }
    
    @Override
    public <T> T applyCredentials(T request) {
        // 应用认证到请求
        return request;
    }
}
```

### 自定义模型适配器

```java
public class CustomModelAdapter extends AbstractModelAdapter {
    
    public CustomModelAdapter(RequestMappingStrategy strategy, AuthProvider authProvider) {
        super(strategy, authProvider);
    }
    
    @Override
    public <T> T convertRequest(ModelRequest request, Class<T> vendorRequestType) {
        // 实现请求转换逻辑
    }
    
    @Override
    public <T> ModelResponse convertResponse(T vendorResponse) {
        // 实现响应转换逻辑
    }
}
```

### 自定义函数格式适配器

```java
public class CustomFunctionAdapter implements FunctionFormatAdapter {
    
    @Override
    public Object formatFunctionsForModel(List<Function> functions) {
        // 将函数格式化为模型可接受的格式
    }
    
    @Override
    public FunctionCall parseFunctionCallFromResponse(ModelResponse response) {
        // 从模型响应中解析函数调用
    }
}
```

### 自定义文档加载器

```java
public class MyCustomDocumentLoader implements DocumentLoader {
    @Override
    public List<Document> loadDocuments(String source) throws DocumentLoadException {
        // 自定义实现
    }
}
```

### 自定义文档分割器

```java
public class MyCustomSplitter implements DocumentSplitter {
    @Override
    public List<Document> split(Document document, SplitterConfig config) {
        // 自定义实现
    }
}
```

### 自定义向量存储

```java
public class ExternalVectorStore implements VectorStore {
    // 实现与外部向量数据库（如Milvus、Qdrant等）的集成
    
    @Override
    public void addDocuments(List<Document> documents, List<float[]> embeddings) {
        // 添加文档和向量到外部存储
    }
    
    @Override
    public List<SearchResult> similaritySearch(float[] queryEmbedding, int topK, Map<String, Object> filter) {
        // 执行向量相似度检索
    }
    
    // 其他方法实现...
}
```

## RAG 配置详解

| 配置项 | 说明 | 默认值 |
|-------|------|--------|
| terra.nova.rag.enabled | 是否启用RAG功能 | true |
| terra.nova.rag.splitting.chunk-size | 文档分块大小 | 1000 |
| terra.nova.rag.splitting.overlap | 分块重叠大小 | 200 |
| terra.nova.rag.splitting.splitter | 分割器类型 | character |
| terra.nova.rag.retrieval.top-k | 默认返回文档数量 | 5 |
| terra.nova.rag.retrieval.minimum-score | 最低相似度阈值 | 0.7 |
| terra.nova.rag.retrieval.rerank | 是否启用重排序 | false |
| terra.nova.rag.context.template | 上下文模板 | 预设模板 |
| terra.nova.rag.context.format-documents | 是否格式化文档 | true |
| terra.nova.rag.vector-store.type | 向量存储类型 | in-memory |
| terra.nova.rag.embedding.model-id | 嵌入模型ID | openai:text-embedding-ada-002 |
| terra.nova.rag.embedding.dimension | 嵌入向量维度 | 1536 |
| terra.nova.rag.embedding.batch-size | 批处理大小 | 20 |
| terra.nova.rag.rerank.enabled | 是否启用重排序功能 | false |
| terra.nova.rag.rerank.type | 重排序器类型 | cross-encoder |
| terra.nova.rag.rerank.model-id | 重排序使用的模型ID | deepseek:deepseek-chat |
| terra.nova.rag.rerank.threshold | 重排序最低分数阈值 | 0.0 |
| terra.nova.rag.rerank.max-documents | 最大重排序文档数量 | 50 |

## RAG 最佳实践

1. 为文档添加丰富的元数据，以便能够进行精确过滤
2. 适当调整分块大小，较小的块适合精确检索，较大的块提供更多上下文
3. 对于大型知识库，考虑使用外部向量数据库如Milvus、Qdrant等
4. 使用高质量的嵌入模型，如OpenAI的text-embedding-3-large
5. 根据应用场景定制上下文模板，使LLM能够更好地理解检索到的信息
6. 启用重排序功能提高检索精度，对于中文场景建议使用DeepSeek模型进行重排序
7. 对于大规模生产环境，建议使用交叉编码器(cross-encoder)模式，提供更好的性能
8. 通过调整重排序阈值(threshold)参数过滤低质量匹配，提高结果质量

## 贡献指南

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。

## 许可证

本项目采用与父项目 Terra Framework 相同的许可证 - 详情请参阅 [LICENSE](../LICENSE) 文件。 