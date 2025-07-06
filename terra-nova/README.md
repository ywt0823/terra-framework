# Terra Nova - AI 模型客户端

Terra Nova 是 Terra 框架中的 AI 模型相关技术模块，提供了对多种大型语言模型的统一访问和管理能力。

## 功能特性

### 🚀 核心功能

- **多模型支持**: 支持 OpenAI、DeepSeek 等多种大型语言模型
- **统一接口**: 实现了 Spring AI 的 ChatClient 接口，提供一致的 API 体验
- **专门化客户端**: 每种模型都有专门的客户端实现，支持模型特有的功能和配置
- **链式调用**: 支持流畅的 API 调用方式，提供良好的开发体验
- **配置灵活**: 支持运行时动态配置模型参数

### 🎯 设计模式

- **策略模式**: 每个 AI 模型作为独立的策略实现
- **代理模式**: ChatClient 作为底层模型的代理，提供增强功能
- **建造者模式**: 支持链式调用创建不同配置的客户端实例

## 客户端实现

### OpenAiChatClient

专门针对 OpenAI 模型的客户端实现，支持 GPT-4、GPT-3.5 等模型。

#### 特性
- 支持 OpenAI 特有的配置选项
- 提供便捷的温度、模型切换方法
- 完整实现 ChatClient 接口

#### 使用示例

```java
// 创建客户端
OpenAiChatClient chatClient = new OpenAiChatClient(openAiChatModel);

// 简单对话
String response = chatClient.getContent("你好，请介绍一下你自己");

// 系统消息 + 用户消息
String response = chatClient.getContent(
    "你是一个专业的Java开发助手",
    "请解释一下Spring Boot的自动配置原理"
);

// 使用自定义选项
OpenAiChatOptions options = OpenAiChatOptions.builder()
    .model(OpenAiApi.ChatModel.GPT_4_O.getValue())
    .temperature(0.3)
    .build();
ChatResponse response = chatClient.call("请写一个Java单例模式示例", options);

// 使用流式 API
String response = chatClient.prompt()
    .user("请解释什么是微服务架构")
    .call()
    .content();

// 链式调用创建新实例
OpenAiChatClient temperatureClient = chatClient.withTemperature(0.9);
String creativeResponse = temperatureClient.getContent("请创作一首关于编程的诗");
```

### DeepSeekChatClient

专门针对 DeepSeek 模型的客户端实现，支持 DeepSeek Chat 等模型。

#### 特性
- 支持 DeepSeek 特有的配置选项
- 优化的中文对话能力
- 完整实现 ChatClient 接口

#### 使用示例

```java
// 创建客户端
DeepSeekChatClient chatClient = new DeepSeekChatClient(deepSeekChatModel);

// 简单对话
String response = chatClient.getContent("你好，请介绍一下你自己");

// 系统消息 + 用户消息
String response = chatClient.getContent(
    "你是一个专业的AI编程助手",
    "请解释一下什么是RESTful API设计原则"
);

// 使用流式 API
String response = chatClient.prompt()
    .system("你是一个资深的软件架构师")
    .user("请解释一下DDD（领域驱动设计）的核心概念")
    .call()
    .content();

// 链式调用
DeepSeekChatClient modelClient = chatClient.withModel("deepseek-chat");
String codeResponse = modelClient.getContent("请写一个Java观察者模式实现");
```

## 配置方式

### Spring Boot 自动配置

在 `terra-autoconfigure` 模块中提供了自动配置支持：

```java
@Configuration
public class TerraOpenaiAutoConfiguration {
    
    @Bean
    @ConditionalOnBean(OpenAiChatModel.class)
    public OpenAiChatClient openAiChatClient(OpenAiChatModel openAiChatModel) {
        return new OpenAiChatClient(openAiChatModel);
    }
}

@Configuration
public class TerraDeepSeekAutoConfiguration {
    
    @Bean
    @ConditionalOnBean(DeepSeekChatModel.class)
    public DeepSeekChatClient deepSeekChatClient(DeepSeekChatModel deepSeekChatModel) {
        return new DeepSeekChatClient(deepSeekChatModel);
    }
}
```

### 配置属性

通过 Spring Boot 配置文件进行模型配置：

```yaml
# OpenAI 配置
spring:
  ai:
    openai:
      api-key: ${OPENAI_API_KEY}
      chat:
        options:
          model: gpt-4o
          temperature: 0.7

# DeepSeek 配置
spring:
  ai:
    deepseek:
      api-key: ${DEEPSEEK_API_KEY}
      chat:
        options:
          model: deepseek-chat
          temperature: 0.7
```

## API 接口

### 基本方法

```java
// 直接调用
ChatResponse call(Prompt prompt);
ChatResponse call(String message);
ChatResponse call(String systemMessage, String userMessage);

// 获取文本内容
String getContent(String message);
String getContent(String systemMessage, String userMessage);

// 链式调用
OpenAiChatClient withOptions(OpenAiChatOptions options);
OpenAiChatClient withTemperature(Double temperature);
OpenAiChatClient withModel(String model);
```

### ChatClient 接口方法

```java
// 流式 API
ChatClientRequestSpec prompt();
ChatClientRequestSpec prompt(String content);
ChatClientRequestSpec prompt(Prompt prompt);

// 构建器
Builder mutate();
```

## 高级功能

### 1. 模型切换

```java
// 运行时切换模型
OpenAiChatClient gpt4Client = chatClient.withModel("gpt-4o");
OpenAiChatClient gpt35Client = chatClient.withModel("gpt-3.5-turbo");
```

### 2. 温度调节

```java
// 创意性回答（高温度）
OpenAiChatClient creativeClient = chatClient.withTemperature(0.9);

// 精确性回答（低温度）
OpenAiChatClient preciseClient = chatClient.withTemperature(0.1);
```

### 3. 复杂配置

```java
// 使用 mutate 创建新配置
String response = chatClient.mutate()
    .defaultSystem("你是一个专业的技术顾问")
    .build()
    .prompt()
    .user("请推荐微服务架构方案")
    .call()
    .content();
```

### 4. 底层模型访问

```java
// 获取底层模型进行复杂操作
OpenAiChatModel underlyingModel = chatClient.getChatModel();
Prompt complexPrompt = new Prompt("复杂的提示...");
ChatResponse response = underlyingModel.call(complexPrompt);
```

## 最佳实践

### 1. 依赖注入

```java
@Service
public class ChatService {
    
    private final OpenAiChatClient openAiChatClient;
    private final DeepSeekChatClient deepSeekChatClient;
    
    public ChatService(OpenAiChatClient openAiChatClient, 
                      DeepSeekChatClient deepSeekChatClient) {
        this.openAiChatClient = openAiChatClient;
        this.deepSeekChatClient = deepSeekChatClient;
    }
    
    public String askQuestion(String question) {
        // 根据需要选择合适的模型
        return openAiChatClient.getContent(question);
    }
}
```

### 2. 错误处理

```java
try {
    String response = chatClient.getContent("你的问题");
    return response;
} catch (Exception e) {
    log.error("调用AI模型失败", e);
    return "抱歉，服务暂时不可用";
}
```

### 3. 性能优化

```java
// 预创建不同配置的客户端实例
@Configuration
public class ChatClientConfig {
    
    @Bean("creativeClient")
    public OpenAiChatClient creativeClient(OpenAiChatModel model) {
        return new OpenAiChatClient(model).withTemperature(0.9);
    }
    
    @Bean("preciseClient")
    public OpenAiChatClient preciseClient(OpenAiChatModel model) {
        return new OpenAiChatClient(model).withTemperature(0.1);
    }
}
```

## 扩展性

### 添加新模型支持

1. 创建新的 ChatClient 实现类
2. 实现 ChatClient 接口
3. 添加自动配置类
4. 添加配置属性

```java
public class NewModelChatClient implements ChatClient {
    // 实现接口方法
}

@Configuration
public class TerraNewModelAutoConfiguration {
    @Bean
    @ConditionalOnBean(NewModelChatModel.class)
    public NewModelChatClient newModelChatClient(NewModelChatModel model) {
        return new NewModelChatClient(model);
    }
}
```

## 总结

Terra Nova 模块提供了完整的 AI 模型客户端解决方案，具有以下优势：

- **统一接口**: 所有模型客户端都实现相同的接口，便于切换和扩展
- **专门优化**: 每个客户端针对特定模型进行优化，发挥最佳性能
- **灵活配置**: 支持多种配置方式，适应不同的使用场景
- **易于扩展**: 设计良好的架构，便于添加新的模型支持
- **Spring 集成**: 与 Spring Boot 深度集成，提供自动配置和依赖注入

通过 Terra Nova，开发者可以轻松地在应用中集成和使用各种大型语言模型，构建智能化的应用程序。 