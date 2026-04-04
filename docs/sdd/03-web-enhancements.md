# Web 层增强

## 设计目标

- 将控制器返回值规范为统一的 `Result` 信封，除非显式豁免。
- 提供默认全局异常处理，将异常映射为 `Result`。
- 通过类 SPI 的 `HeaderCustomizer`，按顺序定制入站请求头相关逻辑。
- 可选记录请求/响应体便于排障（使用 Spring `ContentCachingRequestWrapper` / `ContentCachingResponseWrapper`）。
- 仅在显式开启时注册 CORS 映射。

## 统一响应体（`ResponseAdvice`）

- 类：`com.terra.framework.crust.handler.ResponseAdvice`
- 由 `TerraWebAutoConfiguration` 注册为 Bean（`@ConditionalOnMissingBean`），不依赖宿主应用对 `com.terra.framework` 的组件扫描。
- 机制：`@RestControllerAdvice` + `ResponseBodyAdvice<Object>`
- 行为要点：
  - 若已是 `Result`，原样返回。
  - `ModelAndView`、`Model` 不包装。
  - 方法上标注 `com.terra.framework.crust.annotation.IgnoreResponseAdvice` 时跳过。
  - 其余情况使用 `ResultUtils.success(value)` 包装。

### 配置（`terra.web.response-advice`）

| 属性 | 默认值 | 说明 |
| ---- | ------ | ---- |
| `terra.web.response-advice.enabled` | `true` | 为 `false` 时不注册 `ResponseAdvice`。 |

## 全局异常（`RestExceptionHandler`）

- 类：`com.terra.framework.crust.handler.RestExceptionHandler`
- 由 `TerraWebAutoConfiguration` 注册为 Bean（`@ConditionalOnMissingBean`）。
- 捕获 `Exception`，返回 `ResultUtils.error(ResultEnum.FAIL, message)`。
- 若需要更细错误码，业务侧通常在同类或另一 `@RestControllerAdvice` 中增加 `@ExceptionHandler`，并用 `@Order` 控制优先级。

### 配置（`terra.web.exception-handler`）

| 属性 | 默认值 | 说明 |
| ---- | ------ | ---- |
| `terra.web.exception-handler.enabled` | `true` | 为 `false` 时不注册 `RestExceptionHandler`。 |

## 请求链路：拦截器与 CORS

`TerraWebAutoConfiguration` 会注册：

1. **`RequestHandlerInterceptor`** — 对未命中排除路径的请求，按 `Ordered` 顺序调用各 `HeaderCustomizer` Bean。
2. **WebMvc 配置** — 当 `terra.web.context.enabled` 为 true 时，按 `TerraWebContextExcludeProperties` 排除部分路径。
3. **CORS** — 当 `terra.crust.cors.enabled=true` 时，应用 `TerraCorsProperties`（`mapping`、origins、methods、headers、credentials、`max-age`）。

### 配置（`terra.web.context`）

| 属性 | 默认值 | 说明 |
| ---- | ------ | ---- |
| `terra.web.context.enabled` | `true` | 为 true 时拦截器注册会应用排除路径。 |
| `terra.web.context.excludes` | 逗号分隔的 Ant 模式 | 不经过 `RequestHandlerInterceptor` 的路径。 |

### 配置（`terra.crust.cors`）

| 属性 | 默认值 | 说明 |
| ---- | ------ | ---- |
| `terra.crust.cors.enabled` | `false` | 必须为 true 才注册 CORS。 |
| `terra.crust.cors.mapping` | `/**` | CORS 作用路径。 |
| `terra.crust.cors.allowed-origins` | `*` | 生产环境建议改为明确域名。 |
| `terra.crust.cors.allowed-methods` | GET、POST、PUT、DELETE、OPTIONS、PATCH | 允许的 HTTP 方法。 |
| `terra.crust.cors.allowed-headers` | `*` | 允许的请求头。 |
| `terra.crust.cors.allow-credentials` | `true` | 是否允许携带凭证。 |
| `terra.crust.cors.max-age` | `3600` | 预检缓存秒数。 |

## HTTP 访问日志（`TerraLoggingFilter`）

- 当 `terra.web.logging.enabled=true`（`matchIfMissing` 为 true）且类路径存在 `OncePerRequestFilter` 时创建 Bean。
- 使用 `org.springframework.web.util.ContentCachingRequestWrapper` 与 `ContentCachingResponseWrapper` 缓存体，经 `LogPattern` 打日志。
- 在 `finally` 中调用 `copyBodyToResponse()` 写回真实响应。
- 遵守 `terra.web.logging.exclude-urls`（Ant），并按 `terra.web.logging.max-payload-length`（默认 `4096`）截断。

### 配置（`terra.web.logging`）

| 属性 | 默认值 | 说明 |
| ---- | ------ | ---- |
| `terra.web.logging.enabled` | `true` | 是否注册该过滤器。 |
| `terra.web.logging.exclude-urls` | 静态资源等模式 | 跳过日志的路径。 |
| `terra.web.logging.max-payload-length` | `4096` | 单次记录体最大字符数。 |
| `terra.web.logging.aspectj-expression` | 未设置 | 属性类中预留字段，可按需扩展。 |

## 扩展点

- 声明 `HeaderCustomizer` 类型的 Spring Bean 即可参与拦截器链。
- 若用 `@ConditionalOnMissingBean` 替换 `RequestHandlerInterceptor`，需自行复现依赖的排除与头处理逻辑。

## 与 Trace 的衔接

`TraceIdRestTemplateCustomizer` 与上述 Web 自动配置同块注册；详见 [02-trace-and-logging.md](./02-trace-and-logging.md)。
