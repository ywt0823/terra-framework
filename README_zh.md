# Terra Framework

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

一个功能全面、模块化的，基于 Spring Boot 的框架，旨在加速 Java 企业级应用的开发。Terra Framework 提供了一套生产就绪的模块，用于处理通用的基础设施问题，使开发人员能够专注于业务逻辑。

[English](./README.md)

---

## 核心特性

- **AI 集成**: 即插即用的 RAG (检索增强生成) 和由 Spring AI 驱动的有状态对话能力。
- **Web 层增强**: 统一的响应包装、全局异常处理和分布式追踪。
- **简化的数据访问**: 为 MySQL (JPA) 和 Redis 提供易于配置的 Bean。
- **灵活的缓存**: 抽象的缓存层，并为不同的提供商提供了实现。
- **核心工具集**: 一套丰富的通用工具、自定义异常、日志记录和增强的 HTTP 客户端。

## 模块概览

本框架由多个模块组成，每个模块提供一组不同的功能。

| 模块                                 | 描述                                                                          |
| ------------------------------------ | ----------------------------------------------------------------------------- |
| `terra-dependencies`                 | 通过 `dependencyManagement` 管理框架和所有子项目的依赖版本。                      |
| `terra-bedrock`                      | 提供基础组件，如自定义注解、日志上下文和追踪 ID 生成。                          |
| `terra-sediment`                     | 包含一系列通用工具，如 JSON 助手、自定义异常和结果包装器。                      |
| `terra-crust`                        | 增强 Web 层，提供追踪过滤器、请求日志记录和统一响应处理等功能。                 |
| `terra-strata`                       | 简化如 MySQL/JPA 和 Redis 等技术的数据访问配置。                              |
| `terra-geyser`                       | 提供灵活的缓存抽象和实现 (例如 Redisson)。                                    |
| `terra-nova`                         | 提供前沿的 AI 功能，包括 `RagTemplate` 和 `ConversationTemplate`。              |
| `terra-spring-boot-starter/*`        | 一组 starter 模块，通过自动配置，可以轻松集成到任何 Spring Boot 应用中。        |

## 快速上手

要在您的 Spring Boot 项目中使用 Terra Framework，请遵循以下步骤：

### 1. 添加依赖管理

首先，在您项目的 `pom.xml` 中导入 `terra-dependencies`，以统一管理依赖版本。

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.terra.framework</groupId>
            <artifactId>terra-dependencies</artifactId>
            <version>1.0.0-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

### 2. 添加所需的 Starter

接下来，添加您需要的模块的 starter。例如，要使用 AI 功能：

```xml
<dependencies>
    <!-- AI 功能 (RAG, 对话) -->
    <dependency>
        <groupId>com.terra.framework</groupId>
        <artifactId>terra-nova-spring-boot-starter</artifactId>
    </dependency>

    <!-- Web 层增强 (追踪, 统一响应) -->
    <dependency>
        <groupId>com.terra.framework</groupId>
        <artifactId>terra-crust-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### 3. 配置 `application.properties`

提供必要的配置。对于 AI 模块，您需要设置您的 AI 服务提供商的属性。

```properties
# 启用 Terra AI 模块
terra.ai.enabled=true

# 配置 AI 模型提供商 (例如, DeepSeek)
terra.ai.deepseek.api-key=你的_DEEPSEEK_API_KEY
terra.ai.deepseek.model=deepseek-chat
terra.ai.deepseek.temperature=0.7
```

### 4. 使用模板工具

现在，您可以直接在您的服务中注入并使用自动配置好的模板工具了。

```java
import com.terra.framework.nova.service.RagOperations;
import com.terra.framework.nova.service.ConversationOperations;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class MyAiService {

    private final RagOperations ragTemplate;
    private final ConversationOperations conversationTemplate;

    @Autowired
    public MyAiService(RagOperations ragTemplate, ConversationOperations conversationTemplate) {
        this.ragTemplate = ragTemplate;
        this.conversationTemplate = conversationTemplate;
    }

    public String askAboutTerra(String query) {
        // 使用 RAG 基于知识库提问
        return ragTemplate.ask(query);
    }

    public String haveAConversation(String sessionId, String message) {
        // 进行有状态的对话
        return conversationTemplate.chat(sessionId, message);
    }
}
```

## 贡献

欢迎参与贡献！有关如何为该项目做贡献的详细信息，请参阅 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证

该项目采用 Apache 2.0 许可证。详情请参阅 [LICENSE](LICENSE) 文件。 