# Terra-Stream 模块

Terra-Stream 是一个消息队列增强框架，提供统一的消息处理接口，支持多种消息队列实现。

## 功能特性

- 统一的消息接口，支持不同消息队列的无缝切换
- 支持 RabbitMQ 和 Redis Stream
- 注解驱动的消息发布和消费
- 便捷的API方式访问消息队列
- 与Spring生态系统深度集成

## 快速开始

### 添加依赖

```xml
<dependency>
    <groupId>com.terra.framework</groupId>
    <artifactId>terra-stream</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

### 配置示例

```yaml
terra:
  stream:
    enabled: true
    default-queue-type: rabbitmq  # rabbitmq 或 redis
    
    # RabbitMQ配置
    rabbitmq:
      enabled: true
      durable: true
      exclusive: false
      auto-delete: false
      concurrent-consumers: 1
      max-concurrent-consumers: 10
      prefetch-count: 250
      
    # Redis Stream配置
    redis:
      enabled: true
      max-len: 1000000
      auto-create-group: true
      consumer-name-prefix: consumer-
      consumer-timeout: 1000
      batch-size: 10
      ack-mode: AUTO
      poll-interval: 100
```

### 使用注解方式

```java
// 消费消息
@StreamListener(destination = "orders", group = "order-processors")
public void processOrder(Order order) {
    log.info("处理订单: {}", order);
    // 处理逻辑...
}

// 发布消息
@StreamPublish(destination = "orders")
public Order createOrder(OrderRequest request) {
    Order order = new Order();
    // 设置订单属性...
    return order; // 返回的订单对象会自动发布到消息队列
}
```

### 使用API方式

```java
// 创建生产者
MessageProducer producer = messageQueueFactory.getMessageQueue("rabbitmq").createProducer();

// 发送消息
producer.send("orders", order);

// 创建消费者
MessageConsumer consumer = messageQueueFactory.getMessageQueue("rabbitmq")
    .createConsumer("my-consumer-group");

// 订阅消息
consumer.subscribe("orders", message -> {
    Order order = (Order) message.getPayload();
    // 处理订单...
});
```

## 模块结构

- **core**: 核心接口和类，定义消息处理规范
- **factory**: 消息队列工厂，负责创建不同类型的消息队列
- **annotation**: 注解处理器，处理消息发布和监听注解
- **config**: 配置类和自动配置
- **rabbitmq**: RabbitMQ实现
- **redis**: Redis Stream实现

## 扩展支持

Terra-Stream 设计为可扩展的框架，可以轻松添加新的消息队列实现：

1. 实现 `MessageProducer` 接口创建消息生产者
2. 实现 `MessageConsumer` 接口创建消息消费者
3. 实现 `MessageQueue` 接口整合生产者和消费者
4. 在配置类中注册新的消息队列实现 