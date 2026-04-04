# Terra Framework

[![License](https://img.shields.io/badge/license-Apache%202.0-blue.svg)](LICENSE)

基于 Spring Boot 的轻量级基础设施 Starter，整合原 `terra-bedrock`、`terra-sediment`、`terra-crust` 中与业务无关的通用能力：链路追踪（TraceId / MDC）、统一 API 响应与全局异常、请求日志、JSON 与雪花 ID 等自动配置。

[English](./README.md)

---

## 核心特性

- **链路追踪**：`TraceIdGenerator`、`TraceHelper`、Servlet 过滤器与 `RestTemplate` 透传头。
- **Web 层**：`ResponseAdvice` 统一 `Result` 包装、`RestExceptionHandler`、可选请求/响应体日志（Spring `ContentCaching*`）。
- **通用工具**：`Result` / `ResultUtils`、雪花 ID、布隆过滤器、加解密与常用工具类（已移除未使用的文档解析等大依赖）。

## 模块

| 模块 | 描述 |
| ---- | ---- |
| `terra-springboot-starter` | 单一 Starter，内含自动配置与上述能力。 |

## 快速上手

在业务项目的 `pom.xml` 中加入依赖（版本与当前仓库 `0.0.1-SNAPSHOT` 或你发布后的版本一致）：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-springboot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

Spring Boot 3.4.x 与 Java 21 为当前构建目标。引入依赖后使用常规的 `@SpringBootApplication`（或任何已启用 Spring Boot 自动配置的启动方式）即可，**无需**自定义启动注解；自动配置通过 `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` 注册，各 `terra.*` 配置类已在对应 `@AutoConfiguration` 上通过 `@EnableConfigurationProperties` 绑定。

### 常用配置示例

```properties
terra.web.logging.enabled=true
terra.web.response-advice.enabled=true
terra.web.exception-handler.enabled=true
terra.trace.collector.enabled=true
terra.json.enabled=true
```

## 贡献

请参阅 [CONTRIBUTING.md](CONTRIBUTING.md)。

## 许可证

Apache 2.0，见 [LICENSE](LICENSE)。
