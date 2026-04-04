# 通用工具与 API 契约

## Result 体系（`com.terra.framework.common.result`）

| 类型 | 用途 |
| ---- | ---- |
| `Result<T>` | 通用 API 信封（code、message、data）。 |
| `ResultEnum` | 实现 `ErrorType`；标准 HTTP 对齐码使用 Spring `HttpStatus`，业务系统异常码为 `FAIL`（1000）。 |
| `ResultPageInfo` | 分页元数据。 |
| `ErrorType` | 错误码与文案的契约接口。 |
| `ResultUtils` | 构造成功/失败 `Result` 的工厂方法。 |

因 Starter 依赖 `spring-boot-starter-web`，`ResultEnum` 可直接使用 `spring-web` 的 `HttpStatus`。

## JSON 工具（`com.terra.framework.common.util.common.JsonUtils`）

- 静态门面，提供 `init(ObjectMapper)`，在 Spring Boot 管理的 `ObjectMapper` Bean 就绪后由 `JsonAutoConfiguration` 的 `JsonUtilsInitializer` 调用。
- 当 `terra.json.enabled=true`（默认）时，`JsonAutoConfiguration` 注册 `Jackson2ObjectMapperBuilderCustomizer`，将 `terra.json.*` 合并进 Boot 默认的 Jackson 构建过程，并注册 `JsonUtilsInitializer`。

### 配置（`terra.json`）

| 属性 | 默认值 | 说明 |
| ---- | ------ | ---- |
| `terra.json.enabled` | `true` | 为 `false` 时关闭该自动配置。 |
| `terra.json.date-format` | `yyyy-MM-dd HH:mm:ss` | 通过 customizer 设置 `simpleDateFormat`。 |
| `terra.json.ignore-unknown-properties` | `true` | 对应 Jackson 反序列化是否忽略未知字段。 |
| `terra.json.include-null-values` | `false` | 为 false 时使用 `NON_NULL` 序列化策略。 |
| `terra.json.enable-cache` / `terra.json.cache-size` | `true` / `16` | 属性类已声明；若依赖缓存语义请对照实现是否读取。 |

`JsonAutoConfiguration` 排在 `JacksonAutoConfiguration` 之后，以便在 Boot 已注册 Jackson 基础设施后再追加 Terra 的定制。

## 类雪花 ID（`com.terra.framework.common.util.sequence.SnowflakeUtils`）

- 自动配置加载时，`SnowflakeAutoConfiguration` **总会**注册 `SnowflakeUtils` Bean（`@Bean` 上无 `@ConditionalOnProperty`）；`SnowflakeProperties.enabled` 字段存在但 **尚未**接入配置类逻辑，可视为预留字段。
- `SnowflakeProperties.max-sequence` 配置内部 `SnowflakeSequence` 上限（默认 `10000`）。

### 配置（`terra.snowflake`）

| 属性 | 默认值 | 说明 |
| ---- | ------ | ---- |
| `terra.snowflake.enabled` | `true` | 当前未被 `SnowflakeAutoConfiguration` 使用。 |
| `terra.snowflake.max-sequence` | `10000` | 传给 `SnowflakeSequence`。 |

## 加解密（`com.terra.framework.common.util.encryption`）

- **对称：** `SymmetricalStrategy`、`SymmetricalContext`、`AESUtils`、`PBEUtils`、`SymmetricalMethodEnum`。
- **非对称：** `AsymmetricalStrategy`、`RSAUtils`、`AsymmetricalMethodEnum`。
- 底层使用 JDK 加解密，必要时配合 `commons-codec` 的 Base64。

## 布隆过滤器（`com.terra.framework.common.util.bloomfilter`）

- `BaseBloomFilter<T>` — 基于 Guava `Funnel`、`Hashing` 的参数化布隆过滤器。
- `DefaultBloomFilter` — 具体实现入口。

## 并发

- `AbstractBatchProcess` — 定时批量刷写模式（`TraceDataCollector` 使用）。
- `CustomThreadFactory` — 带命名规则的线程工厂辅助类。

## 其他辅助类

- `PinYinUtils` — 中文转拼音（`jpinyin`）。
- `LocalDateTimeUtils`、`BigDecimalUtils`、`LocalDateParseConstant` — 日期与数值处理。
- `com.terra.framework.crust.web.IpUtil` — 客户端 IP 解析与校验（供 Web 工具与拦截器使用）。
- `LogPattern`、`TermWrapper` — 结构化日志格式（过滤器、收集器使用）。

## 依赖范围（概念）

Starter 已去掉文档解析、额外 HTTP 客户端、第二套 JSON 等重型可选依赖。本层仍较常见的第三方包括：**Guava**（布隆过滤器）、**Commons Lang3**、**Commons Codec**、**jpinyin**。
