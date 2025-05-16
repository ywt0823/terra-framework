# Terra Spring Boot Starter

Terra Spring Boot Starter 是 Terra Framework 的启动器模块，提供了自动配置功能，使开发者能够轻松集成 Terra Framework 的各个组件，实现"开箱即用"的开发体验。

## 核心功能

- **自动配置**：
  - 自动装配 Terra Framework 的各个模块
  - 根据条件激活所需组件
  - 提供默认配置，减少手动配置工作
  - 支持配置覆盖，保证灵活性

- **组件注册**：
  - 自动注册 Terra Framework 组件
  - 管理组件之间的依赖关系
  - 提供组件使用的统一入口
  - 简化应用初始化流程

- **整合支持**：
  - 与 Spring Boot 生态系统无缝集成
  - 支持各种应用类型（Web、批处理、微服务等）
  - 集成常用第三方库
  - 提供统一的配置管理

## 快速开始

### 引入依赖

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-spring-boot-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 基础配置

在 `application.properties` 或 `application.yml` 中配置：

```yaml
terra:
  enabled: true
  
  # 基础组件配置
  bedrock:
    enabled: true
    exception-handler:
      enabled: true
    event:
      enabled: true
  
  # 领域模型配置
  crust:
    enabled: true
    rule-engine:
      enabled: true
  
  # 数据访问配置
  strata:
    enabled: true
    repository:
      enabled: true
  
  # 缓存配置
  geyser:
    enabled: true
    redis:
      enabled: true
  
  # 流处理配置
  stream:
    enabled: true
    rabbitmq:
      enabled: true
  
  # 大语言模型集成配置
  nova:
    enabled: true
    tuner:
      enabled: true
```

### 使用示例

创建一个 Spring Boot 应用：

```java
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import com.terra.framework.bedrock.annotation.EnableTerraExceptionHandler;
import com.terra.framework.geyser.annotation.EnableCaching;
import com.terra.framework.stream.annotation.EnableMessageQueue;
import com.terra.framework.nova.annotation.EnableStellarTuner;

@SpringBootApplication
@EnableTerraExceptionHandler  // 启用统一异常处理
@EnableCaching                 // 启用缓存功能
@EnableMessageQueue           // 启用消息队列功能
@EnableStellarTuner           // 启用参数调优功能
public class MyApplication {
    
    public static void main(String[] args) {
        SpringApplication.run(MyApplication.class, args);
    }
}
```

### 使用REST控制器

```java
import org.springframework.web.bind.annotation.*;
import com.terra.framework.bedrock.core.Result;
import com.terra.framework.bedrock.exception.BusinessException;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {
    
    @Autowired
    private UserService userService;
    
    @GetMapping("/{id}")
    public Result<User> getUser(@PathVariable Long id) {
        User user = userService.findById(id);
        return Result.success(user);
    }
    
    @PostMapping
    public Result<User> createUser(@RequestBody UserCreateRequest request) {
        try {
            User user = userService.create(request);
            return Result.success(user);
        } catch (BusinessException e) {
            return Result.failure(e.getCode(), e.getMessage());
        }
    }
}
```

### 使用缓存

```java
import com.terra.framework.geyser.annotation.Cacheable;

@Service
public class ProductService {
    
    @Autowired
    private ProductRepository productRepository;
    
    @Cacheable(
        cache = "products",
        key = "#id",
        timeToLive = 3600
    )
    public Product getProductById(Long id) {
        return productRepository.findById(id)
            .orElseThrow(() -> new BusinessException("PRODUCT_NOT_FOUND", "产品不存在"));
    }
}
```

### 使用事件总线

```java
import com.terra.framework.bedrock.event.EventBus;
import com.terra.framework.bedrock.event.EventListener;

@Service
public class OrderService {
    
    @Autowired
    private EventBus eventBus;
    
    @Transactional
    public Order createOrder(OrderCreateRequest request) {
        // 业务逻辑
        Order order = orderRepository.save(buildOrder(request));
        
        // 发布事件
        eventBus.publish(new OrderCreatedEvent(order));
        
        return order;
    }
}

@Component
public class OrderEventHandler {
    
    @EventListener
    public void handleOrderCreated(OrderCreatedEvent event) {
        // 处理订单创建事件
        Order order = event.getOrder();
        // 发送通知、更新库存等
    }
}
```

### 使用参数调优

```java
import com.terra.framework.nova.tuner.ParameterTuner;
import com.terra.framework.nova.tuner.TuningContext;
import com.terra.framework.nova.tuner.TuningMetrics;

@Service
public class AIService {
    
    @Autowired
    private ParameterTuner tuner;
    
    public String generateContent(String prompt) {
        // 创建调优上下文
        TuningContext context = TuningContext.builder()
            .taskType(TuningContext.TaskType.GENERATION)
            .optimizationGoal(TuningContext.OptimizationGoal.QUALITY)
            .targetModel("gpt-3.5-turbo")
            .inputText(prompt)
            .build();
        
        // 初始参数
        Map<String, Object> params = new HashMap<>();
        params.put("temperature", 0.7);
        params.put("max_tokens", 200);
        
        // 优化参数
        Map<String, Object> optimizedParams = tuner.tuneParameters(params, context);
        
        // 调用AI模型
        String result = aiClient.generate(prompt, optimizedParams);
        
        // 记录结果指标
        TuningMetrics metrics = TuningMetrics.builder()
            .responseTimeMs(500)
            .tokenCount(150)
            .qualityScore(0.9)
            .build();
        
        // 更新调优器
        tuner.updateWithResult(optimizedParams, context, result, metrics);
        
        return result;
    }
}
```

## 扩展功能

Terra Spring Boot Starter 提供了丰富的扩展点：

- 自定义自动配置类
- 自定义启动器属性
- 集成其他 Spring Boot Starter
- 实现条件化配置

## 配置选项

Terra Spring Boot Starter 支持多种配置方式：

1. **属性配置**：在 `application.properties` 或 `application.yml` 中配置

2. **注解配置**：使用各组件提供的 `@EnableXxx` 注解

3. **Java 配置**：创建自定义的 `@Configuration` 类

4. **编程方式**：通过各模块提供的配置器进行编程配置

完整的配置示例：

```yaml
terra:
  enabled: true
  
  # 基础组件配置
  bedrock:
    enabled: true
    exception-handler:
      enabled: true
      include-stacktrace: never
    security:
      enabled: true
      auth-type: jwt
    event:
      enabled: true
      async: true
  
  # 领域模型配置
  crust:
    enabled: true
    domain:
      event:
        async: true
    rule-engine:
      enabled: true
    statemachine:
      enabled: true
  
  # 数据访问配置
  strata:
    enabled: true
    show-sql: true
    repository:
      enabled: true
    multi-datasource:
      enabled: false
  
  # 缓存配置
  geyser:
    enabled: true
    local:
      enabled: true
      type: caffeine
    distributed:
      enabled: true
      type: redis
  
  # 流处理配置
  stream:
    enabled: true
    default-queue-type: rabbitmq
    producer:
      async: true
    consumer:
      auto-start: true
  
  # 大语言模型配置
  nova:
    enabled: true
    model:
      default-provider: openai
    tuner:
      enabled: true
      default-tuner: bayesian
```

## 贡献指南

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。 