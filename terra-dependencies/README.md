# Terra Dependencies

Terra Dependencies 是 Terra Framework 的依赖管理模块，负责统一管理框架中使用的第三方库和组件版本。

## 主要功能

- **依赖版本统一管理**：集中控制所有模块使用的第三方库版本，避免版本冲突
- **依赖传递关系管理**：合理管理依赖的传递关系，减少冗余依赖
- **兼容性保证**：确保各个版本的库之间相互兼容，提高系统稳定性
- **选择性依赖**：允许子模块按需引入特定依赖，提高灵活性

## 使用方式

将模块引入为父模块:

```xml
<parent>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-dependencies</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <relativePath/>
</parent>
```

或者作为依赖管理:

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.terra.framework</groupId>
            <artifactId>terra-dependencies</artifactId>
            <version>0.0.1-SNAPSHOT</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```

## 管理的依赖

- Spring Boot 及相关模块
- 数据库相关（MySQL、PostgreSQL等）
- ORM工具（MyBatis、Hibernate等）
- 缓存组件（Redis等）
- 消息队列（Kafka、RabbitMQ等）
- 大语言模型SDK（OpenAI、Anthropic等）
- 工具库（Apache Commons、Guava等）
- 测试工具（JUnit、Mockito等）

## 版本更新

当需要更新依赖版本时，只需修改 `terra-dependencies` 模块中的版本号，所有使用该依赖的子模块将自动更新，确保框架整体的版本一致性。

## 贡献指南

欢迎贡献依赖管理相关的改进，特别是：

1. 添加新的有用依赖库
2. 更新现有依赖到更稳定的版本
3. 解决依赖冲突问题
4. 优化依赖管理结构

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。 