# Terra Framework

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

A comprehensive, modular Spring Boot-based framework designed to accelerate the development of enterprise-level applications in Java. Terra Framework provides a suite of production-ready modules that handle common infrastructure concerns, allowing developers to focus on business logic.

🇨🇳 [中文文档](./README_zh.md)

---

## Core Features

- **AI Integration**: Ready-to-use RAG (Retrieval-Augmented Generation) and stateful conversation capabilities powered by Spring AI.
- **Web Layer Enhancement**: Unified response wrapping, global exception handling, and distributed tracing.
- **Simplified Data Access**: Easy-to-configure beans for MySQL (JPA) and Redis.
- **Flexible Caching**: Abstracted caching layer with implementations for different providers.
- **Core Utilities**: A rich set of common utilities, custom exceptions, logging, and an instrumented HTTP client.

## Modules Overview

The framework is organized into several modules, each providing a distinct set of functionalities.

| Module                               | Description                                                                                             |
| ------------------------------------ | ------------------------------------------------------------------------------------------------------- |
| `terra-dependencies`                 | Manages all dependency versions for the framework and inheriting projects via `dependencyManagement`.     |
| `terra-bedrock`                      | Provides foundational components like custom annotations, logging context, and trace ID generation.     |
| `terra-sediment`                     | A collection of common utilities, including JSON helpers, custom exceptions, and result wrappers.       |
| `terra-crust`                        | Enhances the web layer with features like trace filters, request logging, and unified response handling.|
| `terra-strata`                       | Simplifies data access configuration for technologies like MySQL/JPA and Redis.                         |
| `terra-geyser`                       | Offers a flexible caching abstraction and implementations (e.g., Redisson).                             |
| `terra-nova`                         | Provides cutting-edge AI functionalities, including `RagTemplate` and `ConversationTemplate`.           |
| `terra-spring-boot-starter/*`        | A set of starter modules that enable auto-configuration for easy integration into any Spring Boot app.  |

## Quick Start

To use Terra Framework in your Spring Boot project, follow these steps:

### 1. Add Dependency Management

First, import `terra-dependencies` in your project's `pom.xml` to manage dependency versions consistently.

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

### 2. Add Required Starters

Next, add the starters for the modules you need. For example, to use the AI functionalities:

```xml
<dependencies>
    <!-- For AI Capabilities (RAG, Conversation) -->
    <dependency>
        <groupId>com.terra.framework</groupId>
        <artifactId>terra-nova-spring-boot-starter</artifactId>
    </dependency>

    <!-- For Web Layer Enhancements (Tracing, Unified Response) -->
    <dependency>
        <groupId>com.terra.framework</groupId>
        <artifactId>terra-crust-spring-boot-starter</artifactId>
    </dependency>
</dependencies>
```

### 3. Configure `application.properties`

Provide the necessary configurations. For the AI module, you need to set up your AI provider's properties.

```properties
# Enable Terra AI module
terra.ai.enabled=true

# Configure the AI model provider (e.g., DeepSeek)
terra.ai.deepseek.api-key=YOUR_DEEPSEEK_API_KEY
terra.ai.deepseek.model=deepseek-chat
terra.ai.deepseek.temperature=0.7
```

### 4. Use the Templates

Now you can inject and use the auto-configured templates directly in your services.

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
        // Use RAG to ask a question based on a knowledge base
        return ragTemplate.ask(query);
    }

    public String haveAConversation(String sessionId, String message) {
        // Have a stateful conversation
        return conversationTemplate.chat(sessionId, message);
    }
}
```

## Contributing

Contributions are welcome! Please refer to the [CONTRIBUTING.md](CONTRIBUTING.md) for details on how to contribute to this project.

## License

This project is licensed under the Apache 2.0 License. See the [LICENSE](LICENSE) file for details. 

## Framework Enhancement Summary (Recent Improvements)

This section summarizes the key architectural and functional enhancements recently applied to the framework, significantly improving its robustness, flexibility, and production-readiness.

### 1. Decoupling and Dependency Injection (`terra-sediment` & `terra-bedrock`)
- **`JsonUtils` Refactoring**: `JsonUtils` in the `terra-sediment` module has been refactored into a pure utility class, removing its dependency on the Spring framework. It is now initialized via a static `init(ObjectMapper)` method.
- **Safe Initialization**: In `terra-bedrock`, a dedicated `JsonAutoConfiguration` now safely injects the Spring-managed `ObjectMapper` into `JsonUtils` at startup, ensuring decoupling while maintaining convenience.
- **`ILock` Enhancement**: Introduced `NoOpLock`, a no-operation implementation of the `ILock` interface, which serves as a default for environments without a distributed lock, preventing NullPointerExceptions and improving the out-of-the-box experience.

### 2. Pluggable Distributed Tracing (`terra-bedrock` & `terra-crust`)
- **TraceId Generation Abstraction**: Introduced a `TraceIdGenerator` interface in `terra-bedrock`, decoupling the trace ID generation logic from its implementation.
- **Default and Extensible Implementation**: Provided `UUIDTraceIdGenerator` as the default implementation. Using Spring's `@ConditionalOnMissingBean`, users can now easily provide their own `TraceIdGenerator` bean (e.g., for SkyWalking) to override the default behavior.
- **Dependency Injection**: Refactored `TerraTraceFilter` and `TraceHelper` to receive the `TraceIdGenerator` via dependency injection, eliminating hard-coded dependencies.

### 3. Advanced Cache Protection (`terra-geyser`)
- **Cache Security Shield**: Enhanced the caching mechanism with a "security shield" to actively prevent cache breakdown and penetration.
- **Configurable Protection**: Added `breakdownProtection` and `penetrationProtection` flags in `CacheProperties` to enable/disable these features.
- **Implementation Details**:
    - **Breakdown Protection**: When enabled, the `RedissonCacheOperation` uses a distributed lock (`RLock`) to ensure that only one thread queries the database when a cache miss occurs.
    - **Penetration Protection**: Created a serializable `CacheNull` object. If a database query returns `null`, `CacheNull.INSTANCE` is stored in the cache to prevent subsequent requests for the same non-existent data from hitting the database.

### 4. Production-Ready AI Conversation Memory (`terra-nova`)
- **Persistent Conversation History**: Addressed the limitation of the default `InMemoryConversationMemory` by creating `RedisConversationMemory`. This new implementation uses Redis (`RList`) to persist conversation history, ensuring that chat sessions are not lost upon application restart.
- **Seamless Integration & Type Safety**: The implementation was carefully aligned with the latest `Spring AI 1.1.0 GA` standards, letting `Redisson` handle `Message` object serialization directly for improved type safety and code simplicity.
- **Switchable Implementations**: `ConversationMemoryAutoConfiguration` was refactored to be more intelligent. It now uses the `terra.ai.memory.type` property (`in-memory` or `redis`) to conditionally load the appropriate `ConversationMemory` bean, offering flexible configuration for different deployment environments. 

### 5. Dynamic Multi-Datasource Support (`terra-strata`)
The framework now offers powerful, automated support for multiple datasources without requiring any special configuration flags to enable it. It intelligently detects your datasource configurations and wires everything up automatically.

#### a. Configuration
Simply define your datasources under the `spring.datasource` prefix in your `application.yml`. The framework will automatically identify and register them. Use the `primary` key to designate a default datasource.

**Example `application.yml`:**
```yaml
spring:
  datasource:
    # Designate the primary datasource. This is optional if you only have one.
    primary: mysql_db

    # First datasource
    mysql_db:
      url: jdbc:mysql://localhost:3306/db_one
      username: user1
      password: password1
      driver-class-name: com.mysql.cj.jdbc.Driver
      mybatis: # Mybatis-Plus specific configurations
        mapper-locations: classpath:mapper/mysql/*.xml

    # Second datasource
    postgres_db:
      url: jdbc:postgresql://localhost:5432/db_two
      username: user2
      password: password2
      driver-class-name: org.postgresql.Driver
      mybatis:
        mapper-locations: classpath:mapper/postgres/*.xml
```

#### b. Usage
Use the `@TerraMapper` annotation on your Mybatis mapper interfaces to bind them to a specific datasource.

- **`UserMapper.java` (connects to `mysql_db`)**
```java
import com.terra.framework.strata.annoation.TerraDatasource;

@TerraMapper(datasourceName = "mysql_db")
public interface UserMapper {
    // ... methods
}
```

- **`ProductMapper.java` (connects to `postgres_db`)**
```java
import com.terra.framework.strata.annoation.TerraDatasource;

@TerraMapper(datasourceName = "postgres_db")
public interface ProductMapper {
    // ... methods
}
```

Now you can inject and use these mappers in your services, and they will automatically connect to the correct database. 
