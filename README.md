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