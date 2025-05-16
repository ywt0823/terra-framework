# Terra Framework

Terra Framework 是一个现代化的 Java 企业级应用开发框架，专注于大语言模型(LLM)的集成与优化，提供了一套完整的解决方案。

## 核心特性

- **高度模块化**：独立的功能模块，可按需引入
- **灵活配置**：丰富的配置选项，适应不同场景需求
- **易于扩展**：清晰的接口设计，便于自定义实现
- **开箱即用**：Spring Boot Starter 支持自动配置
- **完善监控**：集成 Spring Boot Actuator，提供运行时监控

## 项目结构

Terra Framework 由以下核心模块组成：

| 模块名称 | 描述 | 主要功能 |
|---------|------|----------|
| [terra-dependencies](#terra-dependencies) | 依赖管理模块 | 统一管理所有第三方依赖版本 |
| [terra-bedrock](#terra-bedrock) | 核心基础设施模块 | 异常处理、统一响应、安全框架、事件机制 |
| [terra-nova](#terra-nova) | LLM 集成与优化框架 | 模型路由、参数调优、提示词管理 |
| [terra-crust](#terra-crust) | 业务核心模块 | 领域模型、业务规则、状态机 |
| [terra-strata](#terra-strata) | 数据访问层模块 | ORM支持、事务管理、查询增强 |
| [terra-geyser](#terra-geyser) | 缓存处理模块 | 多级缓存、缓存同步、过期策略 |
| [terra-stream](#terra-stream) | 流处理模块 | 消息队列、事件驱动、流式处理 |
| [terra-sediment](#terra-sediment) | 公共工具模块 | 通用工具类、助手函数 |
| [terra-spring-boot-starter](#terra-spring-boot-starter) | Spring Boot 启动器模块 | 自动配置、便捷集成 |

## 快速开始

### 环境要求

- JDK 17 或更高版本
- Maven 3.8.x 或更高版本
- Spring Boot 3.x

### 添加依赖

使用 Terra Framework 最简单的方式是通过 Spring Boot Starter：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 基本配置

在 `application.properties` 或 `application.yml` 中添加：

```yaml
terra:
  enabled: true
  bedrock:
    enabled: true
  nova:
    enabled: true
    tuner:
      enabled: true
  # 其他模块配置...
```

### 创建应用

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.terra.framework.nova.annotation.EnableStellarTuner;

@SpringBootApplication
@EnableStellarTuner  // 启用参数调优功能
public class MyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

## 模块详情

### terra-dependencies

依赖管理模块，负责统一管理框架使用的第三方库版本，避免版本冲突。

**引入方式**：
```xml
<parent>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-dependencies</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</parent>
```

**或者**：
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

### terra-bedrock

核心基础设施模块，提供框架的基石功能：统一异常处理、响应格式、安全框架、事件机制等。

**引入方式**：
```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-bedrock</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**示例**：
```java
// 使用统一响应格式
@GetMapping("/{id}")
public Result<User> getUser(@PathVariable Long id) {
    User user = userService.findById(id);
    return Result.success(user);
}

// 异常处理
try {
    // 业务逻辑
} catch (Exception e) {
    throw new BusinessException("BUSINESS_ERROR", "业务处理失败");
}

// 事件发布
eventBus.publish(new OrderCreatedEvent(order));
```

### terra-nova

LLM集成与优化框架，提供大语言模型接入、参数调优、提示词管理等功能。

**引入方式**：
```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-nova</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**示例**：
```java
// 参数调优
TuningContext context = tunerService.createContext(
    TaskType.GENERATION,
    "文本生成任务",
    "gpt-3.5-turbo",
    "openai",
    OptimizationGoal.BALANCED,
    "写一篇关于人工智能的短文"
);

Map<String, Object> optimizedParams = tunerService.tuneParameters(
    initialParams, 
    context.getContextId(), 
    "bayesian"
);
```

### terra-crust

业务核心模块，专注于领域模型定义、业务规则和状态机等企业级应用核心功能。

**引入方式**：
```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-crust</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**示例**：
```java
// 使用规则引擎
@Component
public class OrderDiscountRule implements Rule<Order> {
    @Override
    public boolean evaluate(Order order) {
        return order.getTotalAmount().compareTo(new BigDecimal("1000")) >= 0;
    }
    
    @Override
    public void execute(Order order) {
        order.applyDiscount(new BigDecimal("0.1"));
    }
}

// 使用状态机
boolean success = orderStateMachine.trigger(order, OrderEvent.SUBMIT);
```

### terra-strata

数据访问层模块，提供ORM支持、事务管理、动态查询等数据访问功能。

**引入方式**：
```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-strata</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**示例**：
```java
// 使用Repository
@Repository
public interface ProductRepository extends BaseRepository<Product, Long> {
    List<Product> findByStatus(ProductStatus status);
    Page<Product> findByPriceGreaterThan(BigDecimal price, Pageable pageable);
}

// 使用动态查询
Conditions conditions = Conditions.create()
    .like("name", "%" + criteria.getName() + "%")
    .greaterThanOrEqual("price", criteria.getMinPrice());
    
return queryHelper.findAll(productRepository, conditions, pageable);
```

### terra-geyser

缓存处理模块，提供多级缓存、缓存同步等高性能缓存解决方案。

**引入方式**：
```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-geyser</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**示例**：
```java
// 注解方式缓存
@Cacheable(
    cache = "products",
    key = "#id",
    timeToLive = 1800
)
public Product getProductById(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "产品不存在"));
}

// 编程方式缓存
return multiLevelCache.get(
    "users",
    username,
    key -> userRepository.findByUsername(key),
    CacheLevel.ALL
);
```

### terra-stream

流处理模块，提供消息队列集成、事件驱动架构和流式数据处理功能。

**引入方式**：
```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-stream</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**示例**：
```java
// 发送消息
SendResult result = messageProducer.send("order-events", message);

// 消费消息
@MessageHandler(
    topic = "order-events",
    messageType = OrderCreatedMessage.class,
    concurrency = "5"
)
public void handleOrderCreated(OrderCreatedMessage message) {
    // 处理消息
}

// 流处理
streamBuilder.stream("user-activities")
    .filter(activity -> activity.getType() != null)
    .map(activity -> new UserActionEvent(
        activity.getUserId(),
        activity.getType(),
        activity.getTimestamp()
    ))
    .groupBy(UserActionEvent::getUserId)
    .window(Windows.timeWindow(Duration.ofMinutes(5)))
    .aggregate(...)
    .to(new ActivityAlertSink());
```

### terra-sediment

公共工具模块，提供各种通用工具类和助手函数，简化开发过程中的常见任务。

**引入方式**：
```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-sediment</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**示例**：
```java
// 字符串工具
StringUtils.isBlank(str);
StringUtils.capitalize("hello");

// 日期时间工具
Date tomorrow = DateUtils.addDays(date, 1);
String formatted = DateTimeUtils.format(now, "yyyy-MM-dd HH:mm:ss");

// 集合工具
List<Integer> lengths = CollectionUtils.transform(list, String::length);
Map<String, Object> merged = MapUtils.merge(defaults, map);

// 加密工具
String encrypted = AesUtils.encrypt(plainText, key);
String hmacSha256 = DigestUtils.hmacSha256(plainText, hmacKey);
```

### terra-spring-boot-starter

Spring Boot 启动器模块，实现自动配置，简化框架的集成和使用。

**引入方式**：
```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

**配置示例**：
```yaml
terra:
  enabled: true
  bedrock:
    enabled: true
    exception-handler:
      enabled: true
  nova:
    enabled: true
    tuner:
      enabled: true
      default-tuner: bayesian
  geyser:
    enabled: true
    local:
      enabled: true
    distributed:
      enabled: true
  # 更多配置...
```

## StellarTuner 参数优化系统

[StellarTuner](terra-nova/README.md) 是 Terra Framework 的明星组件，专注于大语言模型参数的自动优化。它支持多种优化策略、多目标优化，能根据实际需求智能调整模型参数。

主要特点：
- 支持启发式调优和贝叶斯优化
- 可根据质量、速度、成本或平衡模式进行优化
- 上下文感知，根据任务类型和目标模型自动调整参数
- 实时反馈，基于执行结果动态调整优化策略

详细信息请查看 [terra-nova 模块文档](terra-nova/README.md)。

## 版本说明

当前版本：0.0.1-SNAPSHOT

## 贡献指南

1. Fork 本仓库
2. 创建特性分支 (`git checkout -b feature/amazing-feature`)
3. 提交变更 (`git commit -m 'Add some amazing feature'`)
4. 推送到分支 (`git push origin feature/amazing-feature`)
5. 创建 Pull Request

请参阅 [CONTRIBUTING.md](CONTRIBUTING.md) 了解更多贡献细节。

## 许可证

[Apache License 2.0](LICENSE) 