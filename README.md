# Terra Framework

Terra Framework 是一个现代化的 Java 企业级应用开发框架，提供了一套完整的解决方案。

## 项目结构

- **terra-dependencies**: 依赖管理模块
- **terra-bedrock**: 核心基础设施模块
- **terra-crust**: 业务核心模块
- **terra-strata**: 数据访问层模块
- **terra-geyser**: 缓存处理模块
- **terra-stream**: 流处理模块
- **terra-sediment**: 公共工具模块
- **terra-spring-boot-starter**: Spring Boot 启动器模块

## 快速开始

### 环境要求

- JDK 17 或更高版本
- Maven 3.8.x 或更高版本

### 构建项目

```bash
mvn clean install
```

### 运行测试

```bash
mvn test
```

### 代码质量检查

```bash
mvn verify -P code-quality
```

## 模块说明

### terra-bedrock

基础设施模块，提供框架的核心功能：

- 通用异常处理
- 统一响应格式
- 安全框架集成
- 日志处理

### terra-crust

业务核心模块，包含：

- 业务模型定义
- 领域服务
- 业务规则引擎

### terra-strata

数据访问层模块，提供：

- ORM 支持
- 数据源配置
- 事务管理

### terra-geyser

缓存处理模块，支持：

- 多级缓存
- 分布式缓存
- 缓存同步

### terra-stream

流处理模块，用于：

- 消息队列集成
- 流式处理
- 实时计算

### terra-sediment

公共工具模块，包含：

- 通用工具类
- 辅助函数
- 常量定义

### terra-spring-boot-starter

Spring Boot 启动器，提供：

- 自动配置
- 快速集成
- 开箱即用的功能

## 开发指南

### 代码规范

项目使用 `.editorconfig` 统一代码风格，并通过以下工具确保代码质量：

- Checkstyle
- SpotBugs
- JaCoCo

### 提交规范

提交信息格式：

```
<type>(<scope>): <subject>

<body>

<footer>
```

type 类型：
- feat: 新功能
- fix: 修复
- docs: 文档
- style: 格式
- refactor: 重构
- test: 测试
- chore: 构建

### 分支管理

- master: 主分支，用于发布
- develop: 开发分支
- feature/*: 特性分支
- bugfix/*: 修复分支
- release/*: 发布分支

## 版本说明

当前版本：0.0.1-SNAPSHOT

## 贡献指南

1. Fork 本仓库
2. 创建特性分支
3. 提交变更
4. 推送到分支
5. 创建 Pull Request

## 许可证

[Apache License 2.0](LICENSE)

## 联系方式

- 项目负责人：[Your Name]
- 邮箱：[Your Email] 