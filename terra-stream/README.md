# Terra Stream

Terra Stream 是 Terra Framework 的流处理模块，专注于消息队列集成、事件驱动架构和流式数据处理。它为应用程序提供了强大的异步处理和实时数据流能力。

## 核心功能

- **消息队列集成**：
  - 支持多种消息中间件（Kafka、RabbitMQ、RocketMQ等）
  - 统一的消息发送和消费接口
  - 消息转换和序列化
  - 消息路由和过滤

- **事件驱动架构**：
  - 分布式事件总线
  - 事件发布/订阅机制
  - 事件溯源支持
  - 事件处理器注册与管理

- **流式处理**：
  - 数据流构建与操作
  - 流式转换和聚合
  - 窗口计算
  - 实时分析

- **批量处理**：
  - 批量数据导入/导出
  - 批处理作业管理
  - 任务分片与并行处理
  - 失败恢复机制

- **可靠性保障**：
  - 消息幂等性处理
  - 死信队列管理
  - 消息重试策略
  - 事务消息支持

## 快速开始

### 引入依赖

在项目的 `pom.xml` 中添加以下依赖：

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-stream</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 配置消息队列

在 `application.properties` 或 `application.yml` 中进行配置：

```yaml
terra:
  stream:
    enabled: true
    default-queue-type: rabbitmq  # kafka, rabbitmq, rocketmq
    retry:
      max-attempts: 3
      initial-interval: 1000
      multiplier: 2.0
    dead-letter:
      enabled: true
      queue-suffix: ".dlq"

spring:
  rabbitmq:
    host: localhost
    port: 5672
    username: guest
    password: guest
```

### 定义消息模型

```java
import com.terra.framework.stream.message.Message;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class OrderCreatedMessage extends Message {
    
    private Long orderId;
    private Long userId;
    private BigDecimal amount;
    private LocalDateTime createdTime;
    private List<OrderItemInfo> items;
    
    @Data
    public static class OrderItemInfo {
        private Long productId;
        private Integer quantity;
        private BigDecimal price;
    }
}
```

### 发送消息

```java
import com.terra.framework.stream.producer.MessageProducer;
import com.terra.framework.stream.producer.SendResult;

@Service
public class OrderService {
    
    @Autowired
    private MessageProducer messageProducer;
    
    @Transactional
    public Order createOrder(OrderCreateRequest request) {
        // 业务逻辑
        Order order = orderRepository.save(buildOrder(request));
        
        // 构建消息
        OrderCreatedMessage message = new OrderCreatedMessage();
        message.setOrderId(order.getId());
        message.setUserId(order.getUserId());
        message.setAmount(order.getTotalAmount());
        message.setCreatedTime(order.getCreatedTime());
        
        // 设置消息属性
        message.addAttribute("priority", "high");
        message.addAttribute("source", "web");
        
        // 发送消息
        SendResult result = messageProducer.send("order-events", message);
        log.info("发送订单创建消息: messageId={}, status={}", 
                result.getMessageId(), result.getStatus());
        
        return order;
    }
    
    // 使用事务消息
    public void processLargeOrder(Order order) {
        // 开启事务消息
        messageProducer.beginTransaction();
        
        try {
            // 业务处理
            stockService.reserveStock(order);
            
            // 发送消息1
            OrderProcessingMessage processingMsg = new OrderProcessingMessage();
            processingMsg.setOrderId(order.getId());
            processingMsg.setStatus("PROCESSING");
            messageProducer.send("order-status", processingMsg);
            
            // 发送消息2
            InventoryUpdateMessage inventoryMsg = new InventoryUpdateMessage();
            // 设置消息属性...
            messageProducer.send("inventory-events", inventoryMsg);
            
            // 提交事务
            messageProducer.commitTransaction();
        } catch (Exception e) {
            // 回滚事务
            messageProducer.rollbackTransaction();
            throw e;
        }
    }
}
```

### 消费消息

```java
import com.terra.framework.stream.consumer.MessageConsumer;
import com.terra.framework.stream.consumer.MessageHandler;

// 方式1：注解方式
@Component
public class OrderEventConsumer {
    
    @Autowired
    private OrderService orderService;
    
    @MessageHandler(
        topic = "order-events",
        messageType = OrderCreatedMessage.class,
        concurrency = "5",
        idempotent = true
    )
    public void handleOrderCreated(OrderCreatedMessage message) {
        log.info("收到订单创建消息: orderId={}", message.getOrderId());
        
        // 处理消息
        try {
            // 业务逻辑
            orderService.processNewOrder(message.getOrderId());
        } catch (Exception e) {
            log.error("处理订单创建消息失败", e);
            // 抛出异常将触发重试机制
            throw e;
        }
    }
    
    // 过滤消息的处理器
    @MessageHandler(
        topic = "order-events",
        messageType = OrderCreatedMessage.class,
        filter = "message.amount > 1000",
        concurrency = "2"
    )
    public void handleLargeOrder(OrderCreatedMessage message) {
        log.info("收到大额订单消息: orderId={}, amount={}", 
                message.getOrderId(), message.getAmount());
        
        // 处理大额订单的特殊逻辑
        orderService.processLargeOrder(message.getOrderId());
    }
}

// 方式2：编程方式
@Component
public class InventoryConsumer implements InitializingBean {
    
    @Autowired
    private MessageConsumer messageConsumer;
    
    @Autowired
    private InventoryService inventoryService;
    
    @Override
    public void afterPropertiesSet() {
        // 注册消息处理器
        messageConsumer.register(
            "inventory-events",
            InventoryUpdateMessage.class,
            this::handleInventoryUpdate,
            ConsumerConfig.builder()
                .concurrency(3)
                .idempotent(true)
                .build()
        );
    }
    
    private void handleInventoryUpdate(InventoryUpdateMessage message) {
        // 处理库存更新消息
        inventoryService.updateInventory(
            message.getProductId(),
            message.getQuantityChange()
        );
    }
}
```

### 使用流处理API

```java
import com.terra.framework.stream.flow.StreamBuilder;
import com.terra.framework.stream.flow.Processor;
import com.terra.framework.stream.flow.Sink;

@Service
public class UserActivityAnalytics {
    
    @Autowired
    private StreamBuilder streamBuilder;
    
    public void setupUserActivityPipeline() {
        // 构建流处理管道
        streamBuilder.stream("user-activities")
            // 过滤
            .filter(activity -> activity.getType() != null)
            // 字段提取
            .map(activity -> new UserActionEvent(
                activity.getUserId(),
                activity.getType(),
                activity.getTimestamp()
            ))
            // 分组
            .groupBy(UserActionEvent::getUserId)
            // 窗口聚合
            .window(
                Windows.timeWindow(Duration.ofMinutes(5))
            )
            .aggregate(
                // 初始化聚合状态
                () -> new UserActivitySummary(),
                // 聚合函数
                (key, event, summary) -> {
                    summary.setUserId(key);
                    summary.incrementActionCount(event.getType());
                    summary.updateLastActivityTime(event.getTimestamp());
                    return summary;
                }
            )
            // 过滤聚合结果
            .filter(summary -> summary.getTotalActions() > 10)
            // 结果输出
            .to(new ActivityAlertSink());
    }
    
    // 定义流处理的输出目标
    private static class ActivityAlertSink implements Sink<UserActivitySummary> {
        @Override
        public void accept(UserActivitySummary summary) {
            log.info("用户活动频繁: userId={}, actionCount={}, lastActivity={}",
                    summary.getUserId(),
                    summary.getTotalActions(),
                    summary.getLastActivityTime());
            
            // 发送高频活动警报
            // ...
        }
    }
}
```

## 扩展功能

Terra Stream 提供了丰富的扩展点：

- 自定义消息序列化/反序列化
- 自定义消息路由策略
- 自定义重试策略
- 扩展流处理操作符
- 实现自定义消息中间件连接器

## 配置选项

在 `application.properties` 或 `application.yml` 中可以进行详细配置：

```yaml
terra:
  stream:
    enabled: true
    default-queue-type: kafka
    
    # 消息配置
    message:
      max-size: 1048576  # 最大消息大小（字节）
      compression: true
      serializer: json  # json, protobuf, avro
    
    # 生产者配置
    producer:
      async: true
      batch-size: 16384
      linger-ms: 5
      retry-times: 3
      transaction-timeout: 60000
    
    # 消费者配置
    consumer:
      auto-start: true
      default-concurrency: 3
      max-concurrency: 10
      poll-timeout: 5000
      auto-commit: false
      
    # 重试配置
    retry:
      enabled: true
      max-attempts: 3
      initial-interval: 1000
      max-interval: 10000
      multiplier: 2.0
      retry-on:
        - java.io.IOException
        - java.net.SocketTimeoutException
      
    # 死信配置
    dead-letter:
      enabled: true
      queue-suffix: ".dlq"
      handle-attempts: 1
    
    # 幂等性配置
    idempotent:
      enabled: true
      store-type: redis  # memory, redis, jdbc
      key-generator: default
      expiration: 86400  # 1天
      
    # 监控配置
    monitoring:
      enabled: true
      metrics: true
      tracing: true
      health-check: true
```

## 贡献指南

请参阅 [CONTRIBUTING.md](../CONTRIBUTING.md) 了解我们的代码规范以及提交拉取请求的流程。 