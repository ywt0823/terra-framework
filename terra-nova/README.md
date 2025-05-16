# Terra Nova

Terra Nova 是 Terra Framework 的核心子项目，专注于大语言模型(LLM)的管道和参数优化框架。它基于 Spring Boot 构建，提供了一套用于模型路由、参数优化和提示词管理的完整工具集。

## 核心功能

### StellarTuner 参数优化系统

StellarTuner 是一个专业的大语言模型参数自动优化系统，旨在帮助开发者获得最佳的模型输出效果：

- **多种优化策略**：
  - 启发式调优：基于专家规则的快速参数设置
  - 贝叶斯优化：通过探索与利用平衡自动寻找最优参数
  - 支持扩展自定义优化策略

- **多目标优化**：
  - 质量优先：追求最高质量的输出结果
  - 速度优先：最小化响应时间
  - 成本优先：降低令牌消耗，减少API调用成本
  - 平衡模式：在多个目标间取得平衡

- **上下文感知**：
  - 根据任务类型自动调整参数（聊天、生成、摘要、代码等）
  - 针对不同模型供应商定制化参数调整
  - 支持复杂上下文和历史信息

- **完整的评估体系**：
  - 响应时间监控
  - 令牌使用跟踪
  - 结果质量评分
  - 成本计算
  - 综合得分评估

### NovaGPT 模型路由系统

- 多模型调度
- 成本优化
- 性能优化
- 模型注册与健康监控
- 负载均衡
- 多供应商支持 (OpenAI, Azure OpenAI, Anthropic等)

### Prompt Engine 提示词引擎

- 模板管理
- 版本控制
- 变量插值
- 多语言支持
- 协作编辑

## 快速开始

### 环境要求
- Java 17 或更高版本
- Spring Boot 3.x
- Maven

### 安装

在你的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-nova</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 基本配置

在 `application.properties` 或 `application.yml` 中添加配置：

```properties
# 启用调优功能
terra.nova.tuner.enabled=true

# 选择调优器
terra.nova.tuner.default-tuner=bayesian

# 设置优化目标
terra.nova.tuner.optimization-goal=BALANCED
```

## StellarTuner 使用示例

### 参数优化基本流程

```java
@Autowired
private TunerService tunerService;

// 创建调优上下文
TuningContext context = tunerService.createContext(
    TuningContext.TaskType.GENERATION,
    "文本生成任务",
    "gpt-3.5-turbo",
    "openai",
    TuningContext.OptimizationGoal.BALANCED,
    "写一篇关于人工智能的短文"
);

// 初始参数
Map<String, Object> initialParams = new HashMap<>();
initialParams.put("temperature", 0.7);
initialParams.put("max_tokens", 200);
initialParams.put("top_p", 0.9);

// 优化参数
Map<String, Object> optimizedParams = tunerService.tuneParameters(
    initialParams, 
    context.getContextId(), 
    "bayesian"
);

// 使用优化参数调用模型...
String modelResponse = modelClient.generate(context.getInputText(), optimizedParams);

// 构建指标
TuningMetrics metrics = TuningMetrics.builder()
    .responseTimeMs(450)
    .tokenCount(150)
    .qualityScore(0.85)
    .cost(0.003)
    .build();
metrics.setEndTime(); // 自动计算响应时间

// 更新调优结果
TuningResult result = tunerService.updateWithResult(
    optimizedParams,
    context.getContextId(),
    "bayesian",
    modelResponse,
    metrics,
    false
);

// 如果需要继续优化，重复上述过程...
```

### 自定义调优器

```java
public class CustomParameterTuner implements ParameterTuner {
    @Override
    public Map<String, Object> tuneParameters(Map<String, Object> parameters, 
                                             TuningContext context) {
        // 实现自定义调优逻辑
        Map<String, Object> optimizedParams = new HashMap<>(parameters);
        // 根据context进行参数调整...
        return optimizedParams;
    }
    
    @Override
    public void updateWithResult(Map<String, Object> parameters, 
                                TuningContext context,
                                String result, 
                                TuningMetrics metrics) {
        // 处理调用结果，更新内部状态
    }
    
    @Override
    public String getName() {
        return "custom";
    }
    
    @Override
    public void reset() {
        // 重置调优器状态
    }
}
```

### 通过 Actuator 端点监控

StellarTuner 集成了 Spring Boot Actuator，提供以下端点：

```
GET /actuator/tuner                    # 获取所有调优器
GET /actuator/tuner/context/{contextId} # 获取调优上下文
GET /actuator/tuner/result/{resultId}   # 获取调优结果
POST /actuator/tuner/tune               # 执行参数优化
POST /actuator/tuner/update             # 更新调优结果
POST /actuator/tuner/reset              # 重置调优器
```

## 完整配置参考

```yaml
terra:
  nova:
    tuner:
      enabled: true
      default-tuner: heuristic
      max-iterations: 10
      heuristic:
        enabled: true
      bayesian:
        enabled: true
        initial-exploration-rate: 0.3
        min-exploration-rate: 0.1
      thread:
        core-pool-size: 2
        max-pool-size: 5
        queue-capacity: 100
        thread-name-prefix: tuner-
      actuator:
        enabled: true
        id: tuner
        exposed-over-management: true
        exposed-over-web: true
```

## 贡献指南

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。

## 许可证

本项目采用与父项目 Terra Framework 相同的许可证 - 详情请参阅 [LICENSE](../LICENSE) 文件。 