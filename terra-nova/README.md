# Terra Nova - AI 功能模块

`terra-nova` 模块是 `terra-framework` 的 AI 大脑，基于 Spring AI 框架构建，为开发者提供开箱即用的大语言模型（LLM）应用能力。

## 1. 快速开始

### 1.1. 引入依赖

首先，请确保在您的项目 `pom.xml` 中已包含 `terra-nova` 模块：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-nova</artifactId>
    <version>${project.version}</version>
</dependency>
```

同时，您需要根据您选择使用的 AI 模型，在您的**最终应用**的 `pom.xml` 中添加对应的 Spring AI starter。例如，要使用 DeepSeek 模型，您需要添加：

```xml
<dependency>
    <groupId>org.springframework.ai</groupId>
    <artifactId>spring-ai-deepseek-spring-boot-starter</artifactId>
</dependency>
```
*注意：Spring AI 的版本由 `terra-nova` 模块的 `dependencyManagement` 统一管理，您无需指定版本。*

### 1.2. 添加配置

在您的 `application.properties` 或 `application.yml` 文件中添加以下配置：

```properties
# 启用 Terra AI 功能总开关
terra.ai.enabled=true

# 配置您选择的 AI 模型提供商，以 DeepSeek 为例
spring.ai.deepseek.api-key=YOUR_DEEPSEEK_API_KEY
# spring.ai.deepseek.chat.options.model=deepseek-chat

# （可选）如果您希望使用 Redis 作为向量数据库
# 需要确保 terra-strata 模块中的 Redis 已启用
# terra.redis.enabled=true
```

## 2. 核心功能

通过以上配置，您可以直接在您的代码中注入并使用 `AiService`。

```java
import com.terra.framework.nova.service.AiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MyAiComponent {

    private final AiService aiService;

    @Autowired
    public MyAiComponent(AiService aiService) {
        this.aiService = aiService;
    }

    public void testAi() {
        // 1. 简单聊天
        String response = aiService.chat("给我讲一个关于程序员的笑话");
        System.out.println(response);

        // 2. RAG 问答 (需要先嵌入文档)
        // aiService.embed(new FileSystemResource("path/to/your/document.pdf"));
        String answer = aiService.ask("根据文档，项目的主要目标是什么？");
        System.out.println(answer);
    }
}
```

### 2.1. `AiService` 详解

- **`String chat(String message)`**:
  进行一次简单的聊天对话。

- **`void embed(Resource... resources)`**:
  加载、切分并向量化一个或多个文档，存入配置的 `VectorStore` 中。目前主要支持 PDF 文档。

- **`String ask(String question)`**:
  执行一次 RAG (Retrieval-Augmented Generation) 问答。它会自动在 `VectorStore` 中检索与问题相关的上下文，并基于这些上下文生成答案。

## 3. 高级配置

### 3.1. 配置向量数据库 (VectorStore)

`terra-nova` 模块支持通过条件化配置自动选择 `VectorStore`：

- **默认**: 如果没有其他配置，将使用一个基于**内存**的 `SimpleVectorStore`。这对于测试和简单应用非常方便，但数据不会持久化。
- **Redis**: 如果您的项目中引入了 `terra-strata` 模块，并且在 `application.properties` 中配置了 `terra.redis.enabled=true`，模块将自动切换到使用 `RedisVectorStore`，将向量数据持久化到 Redis 中。

未来将支持更多，如 Elasticsearch。 