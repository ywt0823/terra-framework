# Terra Framework

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

A lightweight Spring Boot starter that bundles infrastructure previously split across `terra-bedrock`, `terra-sediment`, and `terra-crust`: trace IDs and MDC, unified `Result` responses, global exception handling, optional HTTP payload logging, JSON helpers, and snowflake ID auto-configuration.

🇨🇳 [中文文档](./README_zh.md)

---

## Core features

- **Tracing**: `TraceIdGenerator`, `TraceHelper`, servlet filter, and `RestTemplate` header propagation.
- **Web**: `ResponseAdvice`, `RestExceptionHandler`, optional request/response logging using Spring `ContentCaching*` wrappers.
- **Utilities**: `Result` model, snowflake IDs, bloom filters, crypto helpers, and other small utilities (heavy unused dependencies removed).

## Module

| Module | Description |
| ------ | ----------- |
| `terra-springboot-starter` | Single artifact with auto-configuration. |

## Quick start

Add the dependency (match the version you build or publish, e.g. `0.0.1-SNAPSHOT`):

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-springboot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Targets Spring Boot 3.4.x and Java 21. Auto-configuration is registered via `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`. Use a normal `@SpringBootApplication` (or any setup that enables Spring Boot auto-configuration); no custom bootstrap annotation is required. All `terra.*` configuration properties are bound through `@EnableConfigurationProperties` on the auto-configuration classes.

### Example properties

```properties
terra.web.logging.enabled=true
terra.web.response-advice.enabled=true
terra.web.exception-handler.enabled=true
terra.trace.collector.enabled=true
terra.json.enabled=true
```

## Contributing

See [CONTRIBUTING.md](CONTRIBUTING.md).

## License

Apache 2.0. See [LICENSE](LICENSE).
