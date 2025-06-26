# terra-strata

The `terra-strata` module is a dedicated data access layer. It provides configurations, helpers, and enhancements for database operations, primarily designed to work with MyBatis-Plus.

## Features

-   **Database Configuration**: Auto-configures data sources (e.g., Druid).
-   **SQL Monitoring**: Intercepts SQL queries to collect metrics like execution time and frequency.
-   **Data-Aware Cache Invalidation**: Contains specific logic to automatically invalidate caches when it detects that underlying table data has been modified (via insert, update, delete operations in mappers). This feature is automatically enabled when `terra-geyser` is also on the classpath.
-   **Hotspot Caching**: Includes an aspect (`SqlAutoCacheAspect`) that monitors SQL execution and can automatically apply caching to "hotspot" queries that are executed frequently.

## Architecture

This module is designed to be independent of any specific caching implementation. All caching logic is provided by the `terra-geyser` module. `terra-strata` provides the *data-aware enhancement* to the caching layer, enabling intelligent, automatic cache management based on data access patterns.

## 核心功能

- **ORM 支持**：
  - 基于 JPA/Hibernate 的对象关系映射
  - MyBatis 集成
  - 领域对象与数据库表的映射策略

- **多数据源管理**：
  - 动态数据源切换
  - 读写分离
  - 分片路由
  - 多租户数据隔离

- **事务管理**：
  - 声明式事务
  - 编程式事务
  - 分布式事务支持
  - 事务传播行为控制

- **数据访问模式**：
  - Repository 模式实现
  - DAO 模式支持
  - 查询对象(Query Object)模式
  - 规格模式(Specification Pattern)

- **查询增强**：
  - 动态条件查询
  - 分页与排序
  - 自定义 SQL 支持
  - 批量操作优化

- **SQL 自动缓存与热点探测**：
  - 热点 SQL 探测并自动创建缓存
  - 热点表缓存
  - 缓存失效机制（AOP 自动清除 + 手动清除）

- **审计与追踪**：
  - 实体创建/修改时间自动记录
  - 操作用户追踪
  - 数据变更历史（需配合其他模块）

- **Redis 集成**：
  - Redis 缓存管理
  - Redisson 分布式锁
  - 延迟任务处理

- **监控与统计**：
  - SQL 执行指标收集（耗时、错误次数等）
  - 缓存命中率、请求次数等统计信息

- **配置化管理**：
  - YAML 配置支持（数据源、缓存类型、审计开关等）
  - Spring Boot 自动装配

- **扩展性设计**：
  - 多级缓存架构（本地缓存 + Redis）
  - 插件化拦截器（如分页、乐观锁）
  - 自定义 Repository 扩展点

## 快速开始

请参考项目文档或集成示例了解具体使用方式。

## 扩展功能

Terra Strata 提供多种扩展点，满足不同的数据访问需求：

- 自定义 Repository 实现
- 扩展审计功能
- 定制数据源路由策略
- 实现特定数据库的优化
- 集成其他 ORM 框架

## 配置选项

在 `application.properties` 或 `application.yml` 中可以进行详细配置，例如：

```yaml
terra:
  strata:
    # 通用配置
    show-sql: true
    format-sql: true
    batch-size: 100

    # 审计配置
    audit:
      enabled: true
      creator-field: createdBy
      creation-time-field: createdTime
      updater-field: updatedBy
      update-time-field: updatedTime

    # 多数据源配置
    datasources:
      enabled: false
      default: master
      routing-strategy: standard  # standard, tenant, sharding
      datasources:
        master:
          url: jdbc:mysql://master:3306/mydb
          username: root
          password: password
        slave:
          url: jdbc:mysql://slave:3306/mydb
          username: readonly
          password: password
          read-only: true

    # 缓存配置
    cache:
      enabled: true
      type: caffeine  # redis, caffeine, ehcache
      ttl: 3600  # 默认缓存时间（秒）
```

## 贡献指南

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。 