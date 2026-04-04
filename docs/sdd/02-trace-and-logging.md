# 链路追踪与日志

## 设计目标

- 在 Servlet 请求、SLF4J MDC 以及可选的出站 HTTP 调用中传递 **trace id**（及关联的 span 标识）。
- 为 **异步** 执行提供轻量封装，把工作线程与 Trace 上下文对齐。
- 可选地通过 `TraceDataCollector` 对采样后的链路元数据做 **批量日志** 输出。

两层协同工作：

1. **`com.terra.framework.bedrock.trace`** — 与框架无关的追踪原语（`TraceIdGenerator`、`TraceHelper`、`LoggingContextHolder`、异步包装类）。
2. **`com.terra.framework.autoconfigure.crust.*`** — Servlet 过滤器、Spring Bean、HTTP 头透传（`TraceContextHolder`、`TerraTraceFilter`、`TraceIdRequestInterceptor`）。

## Bedrock 追踪层（`com.terra.framework.bedrock.trace`）

| 概念 | 类型 | 职责 |
| ---- | ---- | ---- |
| TraceId 生成 | `TraceIdGenerator` / `UUIDTraceIdGenerator` | 生成 trace 标识（默认无横杠 UUID）。 |
| 静态桥接 | `TraceHelper` | 在 `TraceIdGenerator` Bean 就绪后初始化一次；用 `LoggingContext.MDC_TRACE_KEY` 将 trace id 写入 SLF4J MDC。 |
| 线程内上下文 | `LoggingContextHolder` | 每线程持有 `LoggingContext`（与 Servlet 层 Holder 并存）。 |
| 异步传递 | `TraceRunnable`、`TraceableExecutorService` | 在任务执行前标记子 trace，避免上下文丢失。 |
| CompletableFuture 辅助 | `TerraCompletableFuture` | `supplyAsync` 使用包在 `TraceableExecutorService` 外的执行器；若未通过 `setTaskExecutor` 指定执行器，则使用 `ForkJoinPool.commonPool()`。 |

`TraceHelper` 由 `TerraTraceAutoConfiguration` 内的初始化器 Bean 触发，保证晚于 `TraceIdGenerator` 创建。

## Servlet 与 Spring 集成

### `TraceContextHolder`

- 类型：Spring Bean（仅在 `TerraTraceAutoConfiguration` 中定义一份）。
- 在 **ThreadLocal** 中保存 **trace id、span id、parent span id**，并同步到 **MDC**，键名为：
  - `X-Trace-Id`
  - `X-Span-Id`
  - `X-Parent-Span-Id`
- `getTraceHeaders()` 生成用于出站透传的 Map。
- `clear()` 清理 ThreadLocal 与上述 MDC 键。

### `TerraTraceFilter`

- 以 `FilterRegistrationBean` 注册，优先级较高（相对 Web 配置中的日志过滤器顺序）。
- 命中 `terra.trace.excludes` 的路径跳过。
- 读取请求头 `X-Trace-Id`，若无则通过 `TraceIdGenerator` 生成。
- 生成新的 span id；可选读取 `X-Parent-Span-Id`。
- 将 trace 相关头写回 **响应**；在 `finally` 中清理 `TraceContextHolder`。

### 出站 HTTP（`RestTemplate`）

- `TraceIdRequestInterceptor` 将 `TraceContextHolder` 中的头加入每次客户端请求。
- `TraceIdRestTemplateCustomizer` 在 Boot 注册 `RestTemplate` 定制器时，把该拦截器置于拦截器列表前部。

## 链路数据收集

`TraceDataCollector`（可选 Bean）：

- 在 `terra.trace.collector.enabled=true`（默认 **true**）且无自定义 Bean 替换时注册。
- 使用采样率（`terra.trace.collector.sample-rate`，默认 `1.0`）、有界缓冲（`max-trace-capacity`，默认 `10000`），并继承 `AbstractBatchProcess` 按调度批量刷写。
- 通过 `LogPattern` 输出结构化日志。

## 配置参考（`terra.trace`）

| 属性 | 默认值 | 说明 |
| ---- | ------ | ---- |
| `terra.trace.enabled` | `true` | 供 `TraceDataCollector` 生命周期（`afterPropertiesSet`）使用；**当前不会**关闭 `TerraTraceFilter` 的注册。 |
| `terra.trace.excludes` | 静态资源、Swagger 等 Ant 模式 | `TerraTraceFilter` 跳过的路径。 |
| `terra.trace.collector.enabled` | `true` | 控制是否注册 `TraceDataCollector`（`@ConditionalOnProperty`）。 |
| `terra.trace.collector.sample-rate` | `1.0` | 收集器随机采样比例。 |
| `terra.trace.collector.max-trace-capacity` | `10000` | 收集器在途条目上限。 |

## 扩展点

- 自定义 `@Bean TraceIdGenerator` 可替换默认 UUID 策略（例如对接 OpenTelemetry 或内部 ID 规范）。
- 替换 `TraceContextHolder` 需自行保证与过滤器、拦截器、MDC 的一致性。

## 运维提示

- 若日志无 trace id：确认路径未被 trace 过滤器排除、应用启动成功且 `TraceHelper.init` 已执行、日志 pattern 是否输出业务使用的 MDC 键。
- 异步代码应优先使用 `TraceableExecutorService` 或 `TerraCompletableFuture`（或等价透传方案），避免裸 `Executor` 丢上下文。
