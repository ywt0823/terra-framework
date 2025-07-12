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

## 框架增强总结 (近期改进)

本节总结了近期应用于框架的关键架构和功能增强，这些改进显著提升了其健壮性、灵活性和生产就绪性。

### 1. 解耦与依赖注入 (`terra-sediment` & `terra-bedrock`)
- **`JsonUtils` 重构**: `terra-sediment` 模块中的 `JsonUtils` 已被重构为纯粹的工具类，移除了其对 Spring 框架的依赖。它现在通过一个静态的 `init(ObjectMapper)` 方法进行初始化。
- **安全初始化**: 在 `terra-bedrock` 中，一个专门的 `JsonAutoConfiguration` 现在可以在启动时安全地将 Spring 管理的 `ObjectMapper` 注入到 `JsonUtils` 中，从而在保持便利性的同时实现了松耦合。
- **`ILock` 增强**: 引入了 `NoOpLock`，这是一个 `ILock` 接口的空操作实现，它作为没有分布式锁环境下的默认选项，可以防止空指针异常并改善开箱即用的体验。

### 2. 可插拔的分布式追踪 (`terra-bedrock` & `terra-crust`)
- **TraceId 生成抽象化**: 在 `terra-bedrock` 中引入了 `TraceIdGenerator` 接口，将追踪 ID 的生成逻辑与其实现解耦。
- **默认与可扩展的实现**: 提供了 `UUIDTraceIdGenerator` 作为默认实现。利用 Spring 的 `@ConditionalOnMissingBean`，用户现在可以轻松地提供自己的 `TraceIdGenerator` Bean（例如，用于 SkyWalking）来覆盖默认行为。
- **依赖注入**: 重构了 `TerraTraceFilter` 和 `TraceHelper`，使其通过依赖注入接收 `TraceIdGenerator`，消除了硬编码的依赖。

### 3. 先进的缓存保护 (`terra-geyser`)
- **缓存安全护盾**: 增强了缓存机制，增加了“安全护盾”以主动防止缓存击穿和穿透。
- **可配置的保护**: 在 `CacheProperties` 中添加了 `breakdownProtection` 和 `penetrationProtection` 标志，以启用/禁用这些功能。
- **实现细节**:
    - **击穿保护**: 启用后，`RedissonCacheOperation` 使用分布式锁 (`RLock`) 来确保在缓存未命中时只有一个线程查询数据库。
    - **穿透保护**: 创建了一个可序列化的 `CacheNull` 对象。如果数据库查询返回 `null`，则将 `CacheNull.INSTANCE` 存储在缓存中，以防止后续对相同不存在数据的请求访问数据库。

### 4. 生产就绪的 AI 对话记忆 (`terra-nova`)
- **持久化对话历史**: 通过创建 `RedisConversationMemory` 解决了默认 `InMemoryConversationMemory` 的局限性。这个新实现使用 Redis (`RList`) 来持久化对话历史，确保聊天会话在应用重启后不会丢失。
- **无缝集成与类型安全**: 该实现与最新的 `Spring AI 1.1.0 GA` 标准仔细对齐，让 `Redisson` 直接处理 `Message` 对象的序列化，提高了类型安全性和代码的简洁性。
- **可切换的实现**: `ConversationMemoryAutoConfiguration` 被重构得更加智能。它现在使用 `terra.ai.memory.type` 属性（`in-memory` 或 `redis`）来条件性地加载相应的 `ConversationMemory` Bean，为不同的部署环境提供了灵活的配置。

### 5. 动态多数据源支持 (`terra-strata`)
框架现在为多个数据源提供了强大的自动化支持，无需任何特殊的配置标志来启用它。它能智能地检测您的数据源配置并自动完成所有连接工作。

#### a. 如何配置
只需在您的 `application.yml` 中，在 `spring.datasource` 前缀下定义您的数据源。框架将自动识别并注册它们。使用 `primary` 键来指定一个主数据源。

**`application.yml` 示例:**
```yaml
spring:
  datasource:
    # 指定主数据源。如果只有一个数据源，此项是可选的。
    primary: mysql_db

    # 第一个数据源
    mysql_db:
      url: jdbc:mysql://localhost:3306/db_one
      username: user1
      password: password1
      driver-class-name: com.mysql.cj.jdbc.Driver
      mybatis: # Mybatis-Plus 的特定配置
        mapper-locations: classpath:mapper/mysql/*.xml

    # 第二个数据源
    postgres_db:
      url: jdbc:postgresql://localhost:5432/db_two
      username: user2
      password: password2
      driver-class-name: org.postgresql.Driver
      mybatis:
        mapper-locations: classpath:mapper/postgres/*.xml
```

#### b. 如何使用
在您的 Mybatis mapper 接口上使用 `@TerraMapper` 注解，将它们绑定到特定的数据源。

- **`UserMapper.java` (连接到 `mysql_db`)**
```java
import com.terra.framework.strata.annoation.TerraDatasource;

@TerraMapper(datasourceName = "mysql_db")
public interface UserMapper {
    // ... 方法
}
```

- **`ProductMapper.java` (连接到 `postgres_db`)**
```java
import com.terra.framework.strata.annoation.TerraDatasource;

@TerraMapper(datasourceName = "postgres_db")
public interface ProductMapper {
    // ... 方法
}
```

现在，您可以直接在您的服务中注入并使用这些 mapper，它们将自动连接到正确的数据库。 
